import monitor.* ;
import java.util.Random;

class Barberia extends AbstractMonitor{
	private int n_atendidos = 0;
	private final int num_sillas = 4;
	private int sillas_ocupadas = 0;
	private Condition silla = makeCondition();
	private Condition barbero = makeCondition();
	private Condition sala_espera = makeCondition();

	// invcado por los clientes para cortarse el pelo
	public void cortarPelo (){
		enter();


		if(sillas_ocupadas >= num_sillas){
			System.out.println("No hay sitio en la sala de espera. Un cliente se larga.");
		}
		else{
			if(!silla.isEmpty()){
				System.out.println("El cliente espera en la sala de espera.");
				sillas_ocupadas += 1;
				sala_espera.await();
			}

			if(sillas_ocupadas > 0)
				sillas_ocupadas = sillas_ocupadas - 1;

			if(n_atendidos > 2){
				System.out.println("\t\tEstoy harto de pelar a esta panda de melenudos piojosos");
				n_atendidos = 0;
				barbero.await();
			}

			System.out.println("El barbero comienza a afeitar.");
			n_atendidos += 1;
			barbero.signal();
			silla.await();
	}

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

class barbero_ex{

  public static void main(String[] args){

     	 System.err.println("Hay 4 sillas y 8 clientes.");
	int num_clientes = 8;

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
