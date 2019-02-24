// *****************************************************************************
//
// Prácticas de SCD. Práctica 1.
// Plantilla de código para el ejercicio de los fumadores
//
// *****************************************************************************

#include <iostream>
#include <cassert>
#include <pthread.h>
#include <semaphore.h>
#include <time.h>      // incluye "time(....)"
#include <unistd.h>    // incluye "usleep(...)"
#include <stdlib.h>    // incluye "rand(...)" y "srand"

using namespace std ;

sem_t semaforo_estanquero;
sem_t semaforo_fumadores[3];



// ---------------------------------------------------------------------
// introduce un retraso aleatorio de duración comprendida entre
// 'smin' y 'smax' (dados en segundos)

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

int decide_ingrediente(){
	int ingrediente = rand() % 3;

	retraso_aleatorio( 0.2, 0.8 );

	return ingrediente;
}

void* estanquero(void*){

	int ingrediente;

	while(true){
		ingrediente = decide_ingrediente();

		if(ingrediente == 0){
			cout << "El estanquero pone cerillas en el mostrador." << endl << flush;
		}else if(ingrediente == 1){
			cout << "El estanquero pone tabaco en el mostrador." << endl << flush;
		}else{
			cout << "El estanquero pone papel en el mostrador." << endl << flush;
		}

		sem_post(&semaforo_fumadores[ingrediente]);
		sem_wait(&semaforo_estanquero);
	}

	return NULL;
}

// ----------------------------------------------------------------------------
// función que simula la acción de fumar, como un retardo aleatorio de la hebra.
// recibe como parámetro el numero de fumador
// el tiempo que tarda en fumar está entre dos y ocho décimas de segundo.

void fumar()
{
  
   retraso_aleatorio( 0.2, 0.8 );
   
   
}

void* fumador0(void*){
	while (true) {
        sem_wait(&semaforo_fumadores[0]);
        sem_post(&semaforo_estanquero);

        cout << "El primer fumador coge las cerillas y se va a fumar.\n" << endl << flush;
        fumar();
    }
}

void* fumador1(void*) {
    while (true) {
        sem_wait(&semaforo_fumadores[1]);
        sem_post(&semaforo_estanquero);

        cout << "El segundo fumador coge el tabaco y se va a fumar.\n" << endl << flush;
        fumar();
    }
}

void* fumador2(void*) {
    while (true) {
        sem_wait(&semaforo_fumadores[2]);
        sem_post(&semaforo_estanquero);

        cout << "El tercer fumador coge el papel y se va a fumar.\n" << endl << flush;
        fumar();
    }
}
// ----------------------------------------------------------------------------



// ----------------------------------------------------------------------------

int main()
{
  srand( time(NULL) ); // inicializa semilla aleatoria para selección aleatoria de fumador
  
  pthread_t hebra_fumadores[3];
  pthread_t hebra_estanquero;

  sem_init(&semaforo_estanquero, 0, 0);

  for (int i = 0; i < 3; i++)
        sem_init(&semaforo_fumadores[i], 0, 0);

    pthread_create(&(hebra_fumadores[0]), NULL, fumador0, NULL);
    pthread_create(&(hebra_fumadores[1]), NULL, fumador1, NULL);
    pthread_create(&(hebra_fumadores[2]), NULL, fumador2, NULL);
    pthread_create(&hebra_estanquero, NULL, estanquero, NULL);

    pthread_join(hebra_fumadores[0], NULL);
    pthread_join(hebra_fumadores[1], NULL);
    pthread_join(hebra_fumadores[2], NULL);
    pthread_join(hebra_estanquero, NULL);

    for (int i = 0; i < 3; i++) {
        sem_destroy(&semaforo_fumadores[i]);
    }

    sem_destroy(&semaforo_estanquero);

  return 0 ;
}
