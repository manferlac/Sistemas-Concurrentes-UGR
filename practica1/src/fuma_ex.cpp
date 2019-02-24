// ********************************************************************************************
// FUMADORES
// Solución al problema de los fumadores basada en semáforos.
// Implementado en C/C++ multihebra, usando la funcionalidad de la librería POSIX
// COMPILAR: g++ -o fumadores fumadores.cpp -lrt -lpthread -fpermissive
// OCULTAR WARNINGS -w
//
// Autor: Manuel Fernández La-Chica
// Distemas Concurrentes y Distribuidos
// Departamento de Lenguajes y Sistemas Informáticos, Universidad de Granada
// ********************************************************************************************

#include <iostream>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>                                     // Incluye usleep(...)
#include <stdlib.h>                                     // Incluye rand(...) y srand(...)

using namespace std ;

const int num_fum = 3;

sem_t sem_estanquero;
sem_t sem_estanquero2;     
sem_t sem_fumador[num_fum]; 
sem_t mutex_pantalla;     

void retraso_aleatorio( const float smin, const float smax )
{
  static bool primera = true ;
  if ( primera )        // si es la primera vez:
  {  srand(time(NULL)); //   inicializar la semilla del generador
     primera = false ;  //   no repetir la inicialización
  }
  // calcular un número de segundos aleatorio, entre {\ttbf smin} y {\ttbf smax}
  const float tsec = smin+(smax-smin)*((float)random()/(float)RAND_MAX);
  // dormir la hebra (los segundos se pasan a microsegundos, multiplicándos por 1 millón)
  usleep( (useconds_t) (tsec*1000000.0)  );
}

int fumar() {
   retraso_aleatorio( 0.2, 0.8 ); 
   cout << "Fumando...\n" << endl << flush;
}

void * estanquero( void * e ) {
    while (true){
    	sem_wait(&sem_estanquero);
        int ingrediente = rand() % num_fum;                     // Produce un ingrediente

	        sem_wait(&mutex_pantalla);
	            cout << "Ingrediente producido: " << ingrediente << endl << flush;
	        sem_post(&mutex_pantalla);

        sem_post(&sem_fumador[ingrediente]);
    }
    return NULL ;
}

void * fumador( void *f ) {
    while (true){
        sem_wait(&sem_fumador[(int)f]);

	        sem_wait(&mutex_pantalla);
	            cout << "El fumador " << (int)f << " puede fumar" << endl << flush;
	        sem_post(&mutex_pantalla);

	        fumar();

        sem_post(&sem_estanquero);
        
    }
    return NULL ;
}

int main(int argc, char **argv) { 

	srand( time(NULL) ); // inicializa semilla aleatoria para selección aleatoria de fumador

    sem_init(&sem_estanquero,0,1);
    sem_init(&sem_fumador[num_fum], 0,0);
    sem_init(&mutex_pantalla,0,1);

    pthread_t hebras[1+num_fum];

    pthread_create(&(hebras[0]), NULL, estanquero, NULL);
    for(unsigned i=0; i < num_fum; i++)
        pthread_create(&(hebras[i+1]), NULL, fumador, (void *) i);

    pthread_join(hebras[0], NULL);
    pthread_join(hebras[1], NULL);
    pthread_join(hebras[2], NULL);
    pthread_join(hebras[3], NULL);

    sem_destroy(&sem_estanquero);
    sem_destroy(&sem_fumador[num_fum]);
    sem_destroy(&mutex_pantalla);

    return 0;
}