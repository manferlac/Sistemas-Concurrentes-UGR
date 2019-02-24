#include <iostream>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>                                     // Incluye usleep(...)
#include <stdlib.h>      

using namespace std;

int v1[9], v2[9], x, y, z;

sem_t sem_proceso1;
sem_t sem_proceso2;
sem_t sem_proceso3;

void* proceso1(void*){

	sem_wait(&sem_proceso1);

	for (int i = 0; i < 8; ++i)
	{
		x = v1[i];
		sem_post(&sem_proceso2);
		sem_wait(&sem_proceso1);

	}
}

void* proceso2(void*){
	

	for (int i = 0; i < 8; ++i)
	{
		sem_wait(&sem_proceso2);

		y = v2[i] + x;

		sem_post(&sem_proceso1);

		if (y%2 == 0)
		{
			sem_post(&sem_proceso3);
			sem_wait(&sem_proceso1);
		}
	}
}

void* proceso3(void*){
	z = 0;
	while(){
	sem_wait(&sem_proceso3);
		z += y;
	sem_post(&sem_proceso2);
	}
}

int main(int argc, char const *argv[])
{
	for (int i = 0; i < 8; ++i){
		v1[i] = rand() % 7+1;
		v2[i] = rand() % 7+1;
	}

	cout << "Vectores inicializados con valores aleatorios entre 1 y 7 " << endl << flush;

	sem_init(&sem_proceso1, 0, 1);
	sem_init(&sem_proceso2, 0, 0);
	sem_init(&sem_proceso3, 0, 0);

	pthread_t hebra_proceso1, hebra_proceso2, hebra_proceso3;

	pthread_create(&hebra_proceso1, NULL, proceso1, NULL);
	pthread_create(&hebra_proceso2, NULL, proceso2, NULL);
	pthread_create(&hebra_proceso3, NULL, proceso3, NULL);

	pthread_join(hebra_proceso1, NULL);
	pthread_join(hebra_proceso2, NULL);
	pthread_join(hebra_proceso3, NULL);

	sem_destroy(&sem_proceso1);
	sem_destroy(&sem_proceso2);
	sem_destroy(&sem_proceso3);

	cout << "El valor de la variable z es: " << z << endl << flush;

	return 0;
}