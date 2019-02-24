import monitor.* ;
import java.util.Random;

class Barberia extends AbstractMonitor{
	private Condition silla = makeCondition();
	private Condition barbero = makeCondition();
	private Condition sala_espera = makeCondition();

	// invcado por los clientes para cortarse el pelo
	public void cortarPelo (){
		enter();

		if(!silla.isEmpty()){
			System.out.println("El cliente se queda en la sala de espera (silla ocupada).");
			sala_espera.await();
		}

		System.out.println("El barbero comienza a afeitar a su cliente. - Se ocupa la silla.");
		barbero.signal();
		silla.await();

		leave();
	}

	// invocado por el barbero para esperar (si procede) a un nuevo cliente y sentarlo para el corte
	public void siguienteCliente (){
		enter();

		if(sala_espera.isEmpty() && silla.isEmpty()){
			System.out.println("Silla y sala de esperas vacías. El barbero se pone a dormir.");
			barbero.await();
		}

		System.out.println("El barbero coge a un nuevo cliente.");
		sala_espera.signal();

		leave();
	}

	// invocado por el barbero para indicar que ha terminado de cortar el pelo
	public void finCliente (){
		enter();

		System.out.println("El barbero ha terminado de afeitar al cliente.");
		silla.signal();

		leave();
	}
}

class Barbero implements Runnable{
	private Barberia barberia;
	public Thread thr;

	public Barbero(Barberia la_barberia){
		barberia = la_barberia;
		thr = new Thread(this, "Hebra barbero creada.");
	}

	public void run (){
		while (true) {
			barberia.siguienteCliente ();
			aux.dormir_max( 2500 ); // el barbero está cortando el pelo
			barberia.finCliente ();
		}
	}
}

class Cliente implements Runnable{
	private Barberia barberia;
	public Thread thr;

	public Cliente(Barberia la_barberia){
		barberia = la_barberia;
		thr = new Thread(this, "Hebra barbero creada.");
	}

	public void run (){
		while (true) {
			barberia.cortarPelo (); // el cliente espera (si procede) y se corta el pelo
			aux.dormir_max( 2000 ); // el cliente está fuera de la barberia un tiempo
		}
	}
}

class aux{
	static Random genAlea = new Random() ;

	static void dormir_max( int milisecsMax ){
		try{
			Thread.sleep( genAlea.nextInt( milisecsMax ) ) ;

		}catch( InterruptedException e ){
			System.err.println("sleep interumpido en ’aux.dormir_max()’");
		}
	}
}

class BarberoSillas{

  public static void main(String[] args){

  	if ( args.length != 1 ){
      System.err.println("Hay que especificar el número de clientes.");
      return;
    }

  	int num_clientes = Integer.parseInt(args[0]);

  	// Declaración de las hebras
  	Barberia barberia = new Barberia();
  	Barbero barbero = new Barbero(barberia);
  	Cliente[] clientes = new Cliente[num_clientes];


  	// Inicializamos las hebras
  	barbero.thr.start();

  	for(int i = 0; i < num_clientes; i++){
		clientes[i] = new Cliente(barberia);
  		clientes[i].thr.start();
  	}
  }
}
