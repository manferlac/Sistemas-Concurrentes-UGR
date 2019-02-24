
import monitor.*;

class Buffer extends AbstractMonitor {

  private int numeroSlots = 0;
  private volatile int contador = 0;
  private double[] buffer = null;
  private final Condition puede_depositar = makeCondition();
  private final Condition puede_extraer = makeCondition();

  public Buffer(int numeroSlots){
    this.numeroSlots = numeroSlots;
    this.buffer = new double[this.numeroSlots];
  }

  public void depositar(double valor){
    enter(); //Cualquier hebra que llame a este método es retrasado hasta que el monitor este desocupado. Al regresar de este método, el monitor se considera ocupado.

    if (this.contador == this.numeroSlots) {
        this.puede_depositar.await();
    }

    this.buffer[this.contador] = valor;
    this.contador++;

    this.puede_extraer.signal();

    leave();
  }

  public double extraer(){

    double valor;
    enter();

    if (this.contador == 0) {
      this.puede_extraer.await();
    }

    this.contador--;
    valor = this.buffer[this.contador];

    this.puede_depositar.signal();

    leave();

    return valor;
  }
}

  class Productor implements Runnable{
    private Buffer buf = null;
    private int veces;

    public Productor(Buffer buf, int veces){
      this.buf = buf;
      this.veces = veces;
    }

    public void run(){
      String str = Thread.currentThread().getName();
      double item = 100 * Integer.parseInt(str);
      int i = 0;

      while(i++ < this.veces){
        System.out.println("Produciendo " + item);
        this.buf.depositar(++item);

      }
    }
  }

  class Consumidor implements Runnable{
    private Buffer buf = null;
    private int veces;

    public Consumidor(Buffer buf, int veces){
      this.buf = buf;
      this.veces = veces;
    }

    public void run(){
      String str = Thread.currentThread().getName();
      double item;
      int i = 0;

      while (i++ < this.veces) {
        item = this.buf.extraer();
        System.out.println(Thread.currentThread().getName() + " consumiendo " + item);
      }
    }
  }

  public class ProductorConsumidor{

    public static void main(String[] args) {
        if (args.length != 5) {
          System.err.println("Uso: num_cons num_prods tam_buf n_iter_prod n_iter_cons");
        }else{
          try{
            int iteraciones_consumidor = Integer.parseInt(args[3]);
            int iteraciones_productor = Integer.parseInt(args[4]);

            Buffer buf = new Buffer(Integer.parseInt(args[2])); //Creamos buffer con el tamaño pasado por parametro
            Thread[] consumidoras = new Thread[Integer.parseInt(args[0])];

            for (int i = 0; i<consumidoras.length ; i++ ) {
                consumidoras[i] = new Thread(new Consumidor(buf,iteraciones_consumidor), "Consumidor " + (i+1));
            }

            Thread[] productoras = new Thread[Integer.parseInt(args[1])];

            for (int i = 0; i < productoras.length ; i++ ) {
                productoras[i] = new Thread(new Productor(buf,iteraciones_productor), "" + (i+1));
            }

            for (int i = 0; i < productoras.length; i++) {
                productoras[i].start();
            }

            for (int i = 0; i < consumidores.length; i++) {
                consumidoras[i].start();
            }
          }catch(Exception e){
            System.err.println("Excepcion en main: " + e);
          }
        }
    }
  }
