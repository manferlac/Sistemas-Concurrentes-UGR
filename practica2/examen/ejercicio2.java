import monitor.* ;
import java.util.Random;

class Peaje extends AbstractMonitor{
	private int cabina1 = 0,
				cabina2 = 0;

	private Condition cola_cabina1 = makeCondition();
	private Condition cola_cabina2 = makeCondition();

	// invcado por los coches para hacer cola
	public int llegada_peaje(){
		enter();

		int x = 1;

		//if(!cola_cabina1.isEmpty() && !cola_cabina2.isEmpty()){
			if(cabina1 <= cabina2){
				System.out.println("Coche llega a cabina 1.");

				cabina1 += 1;
				if(cabina1 > 1){
					System.out.println("Coche ESPERANDO en cola 1.");
					cola_cabina1.await();
				}

			}
			else{
				System.out.println("Coche llega a cabina 2.");
				x = 2;

				cabina2 += 1;
				if(cabina2 > 1){
					System.out.println("Coche ESPERANDO en cola 2.");
					cola_cabina2.await();
				}
			}

		leave();

		return x;
	}

	public void pagado(int cab){
		enter();

		if(cab == 1){
			cabina1 = cabina1 - 1;
			System.out.println("\t\t\tCoche paga y sale de cabina 1.");
			cola_cabina1.signal();
		}
		else{
			System.out.println("\t\t\tCoche paga y sale de cabina 2.");
			cabina2 = cabina2 - 1;
			cola_cabina2.signal();
		}

		leave();
	}
}

class Hebra_coche implements Runnable{
	private int cabina;
	private Peaje peaje;
	public Thread thr;

	public Hebra_coche(Peaje peaje){
		this.peaje = peaje;
		thr = new Thread(this, "Hebra coche creada.");
	}

	public void run (){
		while (true) {
			cabina = peaje.llegada_peaje();
			aux.retardo( 2000 );
			peaje.pagado(cabina);
			aux.retardo( 2000 );
		}
	}
}

class aux{
	static Random genAlea = new Random() ;

	static void retardo( int milisecsMax ){
		try{
			Thread.sleep( genAlea.nextInt( milisecsMax ) ) ;

		}catch( InterruptedException e ){
			System.err.println("retardo interumpido en ’aux.retardo()’");
		}
	}
}

class ejercicio2{

  public static void main(String[] args){

  	if ( args.length != 1 ){
      System.err.println("Hay que especificar el número de coches.");
      return;
    }

  	int num_coches = Integer.parseInt(args[0]);

  	// Declaración de las hebras
  	Peaje peaje = new Peaje();
  	Hebra_coche[] coche = new Hebra_coche[num_coches];

  	// Inicializamos las hebras
  	for(int i = 0; i < num_coches; i++){
		coche[i] = new Hebra_coche(peaje);
  		coche[i].thr.start();
  	}
  }
}
