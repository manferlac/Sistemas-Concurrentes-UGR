// *****************************************************************************
//
// Prácticas de SCD. Práctica 1.
// Plantilla de código para el ejercicio del productor-consumidor con
// buffer intermedio.
// COMPILAR: g++ -o prod-cons prod-cons.cpp -lrt -lpthread
// *****************************************************************************

#include <iostream>
#include <cassert>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h> // necesario para {\ttbf usleep()}
#include <stdlib.h> // necesario para {\ttbf random()}, {\ttbf srandom()}
#include <time.h>   // necesario para {\ttbf time()}

using namespace std ;

// ---------------------------------------------------------------------
// constantes configurables:

const unsigned
  num_items  = 100 ,    // numero total de items que se producen o consumen
  tam_vector = 5 ;    // tamaño del vector, debe ser menor que el número de items

// Atributos

  int buffer[tam_vector];	//Buffer donde vamos a almacenar los datos producidos
  int indice = 0;			//Indice del buffer donde se ha almacenado el dato producido


//Semáforos

  sem_t puede_producir;		//Semáforo que controla el productor
  sem_t puede_consumir;		//Semáforo que controla al consumidor
  sem_t mutex;				//Semáfoto para que no se solapen los cout



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

// ---------------------------------------------------------------------
// función que simula la producción de un dato

unsigned producir_dato()
{
  static int contador = 0 ;
  contador = contador + 1 ;
  retraso_aleatorio( 0.1, 0.5 );
  //cout << "Productor : dato producido: " << contador << endl << flush ;
  return contador ;
}
// ---------------------------------------------------------------------
// función que simula la consumición de un dato

void consumir_dato( int dato )
{
   retraso_aleatorio( 0.1, 1.5 );
   cout << "Consumidor:                              dato consumido: " << dato << endl << flush ;
}
// ---------------------------------------------------------------------
// función que ejecuta la hebra del productor

void * funcion_productor( void * )
{
  for( unsigned i = 0 ; i < num_items ; i++ )
  {
    

    sem_wait(&puede_producir);
    int dato = producir_dato() ;
    sem_wait(&mutex);

    //Insertamos el dato en el vector    
    
    buffer[indice] = dato;
    cout << "Productor : dato insertado: " << dato << endl << flush ;
    indice++;

    sem_post(&mutex);
    sem_post(&puede_consumir);

    
  }
  return NULL ;
}
// ---------------------------------------------------------------------
// función que ejecuta la hebra del consumidor

void * funcion_consumidor( void * )
{
  for( unsigned i = 0 ; i < num_items ; i++ )
  {

  	sem_wait(&puede_consumir);
  	sem_wait(&mutex);

    int dato ;

    // falta aquí: leer "dato" desde el vector intermedio
    dato = buffer[indice-1];
    consumir_dato( dato ) ;
    indice--;

    sem_post(&mutex);
    sem_post(&puede_producir);
    
  }
  return NULL ;
}
//----------------------------------------------------------------------

int main()
{

  //Iniciamos los semáforos

	sem_init(&puede_producir, 0, tam_vector);	//inicialmente se puede producir, hasta el tam_vector
	sem_init(&puede_consumir, 0, 0);			//inicialmente no se puede consumir ya que no hay datos producidos
	sem_init(&mutex, 0, 1);						//semaforo para la exclusion mutua

	//Creamos las hebras

	pthread_t hebra_productora, hebra_consumidora;

	pthread_create(&hebra_productora, NULL, funcion_productor, NULL);
	pthread_create(&hebra_consumidora, NULL, funcion_consumidor, NULL);

	//Lanzamos las hebras

	pthread_join(hebra_productora, NULL);
	pthread_join(hebra_consumidora, NULL);

	//Destruimos semaforos

	sem_destroy(&puede_producir);
	sem_destroy(&puede_consumir);
	sem_destroy(&mutex);

	cout << "\nFin" << endl << flush;

   return 0 ;
}
