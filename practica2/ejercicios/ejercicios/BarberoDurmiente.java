//Ejercicio de los Fumadores
//Alumno: Manuel Fernández La-Chica

import monitor.*;
import java.util.Random;

class Barberia extends AbstractMonitor
{
    private Condition sala_espera   = makeCondition();
    private Condition barbero       = makeCondition();
    private Condition silla         = makeCondition();

    // invocado por los clientes para cortarse el pelo
    public void cortarPelo()
    {
        enter();

            // Si las silla está ocupada...
            if (!silla.isEmpty())
            {
                // .. pasamos el cliente a la sala de espera
                System.out.println("\033[34m"+"Entra un nuevo cliente a sala de espera (Silla ocupada) " + "\033[0m");
                sala_espera.await();

            }

            // Pelamos al cliente (despertamos al barbero y ponemos al cliente en la silla)
            System.out.println("\033[33m"+"Cliente empieza a afeitarse cuando la silla de queda VACIA" + "\033[0m");
            barbero.signal();
            silla.await();

        leave();
    }
    // invocado por el barbero para esperar (si procede) a un nuevo cliente y sentarlo para el corte
    public void siguienteCliente()
    {
        enter();

            // Si la sala y la silla están vacias...
            if (sala_espera.isEmpty() && silla.isEmpty())
            {
                // ...ponemos al barbero a dormir
                System.out.println("\033[31m"+"No hay clientes y el Barbero se pone a dormir " + "\033[0m");
                barbero.await();

            }


            // Sacamos al siguiente cliente de la sala de espera
            System.out.println("\033[32m"+"Se sienta un nuevo cliente en la silla ,Barbero lo atiende " + "\033[0m");
            sala_espera.signal();

        leave();
    }

    // invocado por el barbero para indicar que ha terminado de cortar el pelo
    public void finCliente()
    {
        enter();

            // Sacamos al cliente de la silla (estaba en espera la hebra mientras se pelaba)
            System.out.println("\033[31m"+"Barbero termina de afeitar al cliente actual " + "\033[0m");
            silla.signal();

        leave();
    }
}

class Cliente implements Runnable
{
    private Barberia barberia;
    public Thread thr;
    public Cliente(Barberia mon)
    {
        barberia = mon;
        thr   = new Thread(this,"Hebra cliente creada ");

    }

    public void run()
    {
        while (true)
        {
            try
            {
                barberia.cortarPelo();  // el cliente espera (si procede) y se corta el pelo
                auxBarbero.dormir_max( 2000 ); // el cliente esta fuera de la barberia un tiempo
            }
            catch(Exception e)
            {
                System.err.println("Excepcion en main: " + e);
            }

        }
    }
}

class Barbero implements Runnable
{
    private Barberia barberia;
    public Thread thr;
    public Barbero(Barberia mon)
    {
        barberia = mon;
        thr   = new Thread(this,"Hebra barbero creada");
    }

    public void run()
    {
        while (true)
        {
            try
            {
                barberia.siguienteCliente();
                auxBarbero.dormir_max( 2500 ); // el barbero esta cortando el pelo
                barberia.finCliente();
            }
            catch(Exception e)
            {
                System.err.println("Excepcion en main: " + e);
            }

        }
    }
}

class auxBarbero
{
    static Random genAlea = new Random();
    static void dormir_max(int milisecsMax)
    {
        try
        {
            Thread.sleep(genAlea.nextInt(milisecsMax));
        }
        catch(InterruptedException e)
        {
            System.err.println("sleep interumpido en 'aux.dormir_max()'");
        }
    }
}

class MainBarberia
{
    public static void main(String[] args)
    {
        final int NUM_CLIENTES = 5;

        // leer parametros, crear vectores y buffer intermedio
        Barberia barberia = new Barberia();

        // crear hebras
        Barbero barbero = new Barbero(barberia);
        Cliente[] clientes = new Cliente[NUM_CLIENTES];
        for (int i=0; i<NUM_CLIENTES; i++)
            clientes[i] = new Cliente(barberia);


        // poner en marcha las hebras
        barbero.thr.start();

        for (int i=0; i<NUM_CLIENTES; i++)
            clientes[i].thr.start();

        auxBarbero.dormir_max( 8500 ); // el barbero esta cortando el pelo

    }
}
