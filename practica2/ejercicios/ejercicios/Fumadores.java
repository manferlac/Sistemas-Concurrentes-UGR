//Ejercicio de los Fumadores
//Alumno: Manuel Fernández La-Chica

import monitor.*;

class Estanco extends AbstractMonitor{

  private int estado;
  private Condition[] fumador = new Condition[3];
  private Condition estanquero;

  public Estanco(){
    this.estado = -1;

    for (int i = 0;i<this.fumador.length ;i++ ) {
        this.fumador[i] = makeCondition();
    }

    this.estanquero = makeCondition();
  }

  public void obtenerIngrediente(int miIngrediente){
    enter();

    if (estado != miIngrediente) {
        this.fumador[miIngrediente].await();
    }

    if (miIngrediente == 0) {
        System.out.println("El primer fumador coge el tabaco y las cerillas y se va a fumar");
    }else if(miIngrediente == 1){
        System.out.println("El segundo fumador coge el papel y las cerillas y se va a fumar");
    }else{
        System.out.println("El tercer fumador coge el tabaco y el papel y se va a fumar");
    }

    this.estado = -1;

    this.estanquero.signal();
    leave();
  }

  public void ponerIngrediente(int ingrediente){
      enter();

      if (ingrediente == 0) {
        System.out.println("El estanquero pone Tabaco y Cerillas en la mesa");
        this.estado = 0;
      }else if (ingrediente == 1) {
        System.out.println("El estanquero pone Papel y Cerillas en la mesa");
        this.estado = 1;
      }else{
        System.out.println("El estanquero pone Tabaco y Papel en la mesa");
        this.estado = 2;
      }

      this.fumador[estado].signal();
      leave();
  }

  public void esperarRecogidaIngrediente(){
    enter();

      if (this.estado != -1)
          this.estanquero.await();

      System.out.println("La mesa del estanco está vacia");

    leave();
  }
}

class Fumador implements Runnable{

  private int miIngrediente;
  private Estanco estanco;

  public Fumador(int miIngrediente, Estanco estanco){
    this.miIngrediente = miIngrediente;
    this.estanco = estanco;
  }

  public void run(){
    while(true){
        this.estanco.obtenerIngrediente(this.miIngrediente);
        System.out.println(Thread.currentThread().getName() + " está fumando...");
        aux.dormir_max(2000);
        System.out.println(Thread.currentThread().getName() + " HA TERMINADO DE FUMAR...");
    }
  }
}

class Estanquero implements Runnable{
  private Estanco estanco;

  public Estanquero(Estanco estanco){
    this.estanco = estanco;
  }

  public void run(){
    int ingrediente;
    while (true){
      ingrediente = (int) (Math.random () * 3.0); //0,1 ó 2
      estanco.ponerIngrediente(ingrediente);
      estanco.esperarRecogidaIngrediente();
    }
  }
}

public class Fumadores{
  public static void main(String[] args){
    Estanco estanco = new Estanco();
    Thread hebraEstanquero = new Thread(new Estanquero(estanco));
    Thread[] hebraFumador = new Thread[3];

    hebraFumador[0] = new Thread(new Fumador(0, estanco), "El primer fumador");
    hebraFumador[1] = new Thread(new Fumador(1, estanco), "El segundo fumador");
    hebraFumador[2] = new Thread(new Fumador(2, estanco), "El tercer fumador");

    hebraEstanquero.start();

    for (int i = 0; i < hebraFumador.length; i++) {
        hebraFumador[i].start();
    }
  }
}
