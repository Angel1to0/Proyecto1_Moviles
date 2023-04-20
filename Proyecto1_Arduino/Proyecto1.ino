
#include <SoftwareSerial.h>
#include <Tone.h> //Para emitir sonidos

SoftwareSerial BT(10,11); // pines del modulo bluetooth

byte led_1 = 3; //pin para leds
byte led_2 = 4; //pin para leds
byte led_3 = 5; //pin para leds
byte speaker = 6; //pin para la señal de sonido
Tone tono; //objeto para la generacion de sonidos

char CharIN = ' '; //comando recibido desde el telefono

//Inicializo todos los componentes necesarios
void setup()  
{
  Serial.begin(9600); //establesco la comunicacion a 9600 baudios
  BT.begin(9600); 
  IniciarPines(); //funcion para inciar pines
  tono.begin(speaker); //inicializo el objeto para genera sonidos
  Serial.println("Iniciando Control ...");
}
 
void loop()
{      
    if(BT.available())
    {
      CharIN = BT.read();
      Serial.print(CharIN);
      OnOffLeds();
    }
}

void IniciarPines (void)
{
  pinMode(led_1, OUTPUT);
  pinMode(led_2, OUTPUT);
  pinMode(led_3, OUTPUT);
  pinMode(speaker, OUTPUT); //inicialo el pin para salida de sonido
 
  digitalWrite(led_1, LOW); 
  digitalWrite(led_2, LOW);
  digitalWrite(led_3, LOW);
  digitalWrite(speaker, LOW); //inizializar pin de sonido
}

void OnOffLeds (void)
{
  if(CharIN == 'A'){digitalWrite(led_1, HIGH);}
  if(CharIN == 'B'){digitalWrite(led_1, LOW);}

  if(CharIN == 'C'){digitalWrite(led_2, HIGH);}
  if(CharIN == 'D'){digitalWrite(led_2, LOW);}

  if (CharIN == 'E') {ejecutarSOS();}
}

void ejecutarSOS(){
  while (CharIN == 'E') {
    ciclos(led_2, 500, 300);
    retardos();
    ciclos(led_1, 1500, 900);
    retardos();
    ciclos(led_3, 500, 300);
    BT.write("E");
    if(BT.available()){
      CharIN = BT.read();
      if(CharIN == 'F'){
        apagarSOS();
        BT.write("F");
        tono.stop();
        break;
      }
    }
    delay(1500);
  }
}

void apagarSOS(){
  digitalWrite(led_1, LOW);
  digitalWrite(led_2, LOW);
  digitalWrite(led_3, LOW);
}

//Funcion que hace la repeticion de 3 veces, el delay es para que tarde un poco mas en llamar a la funcion de 'cortos'
void ciclos(byte ledPin, int s, int d){
  for(int i=0; i<3; i++){
    retardos();
    cortos(ledPin,s,d);
  }
}

//recibe el pin que debe mandar la señal, y el tiempo del delay
void cortos(byte ledPin, int s, int d) {
  digitalWrite(ledPin, HIGH); // Encender el LED
  tono.play(NOTE_C4,d);
  delay(s); // Esperar 2 segundos
  //tono.play(NOTE_C4,d); Este segundo tono, aun no estoy seguro de porque lo agregue, pero el comentarlo no afecta en nada
  digitalWrite(ledPin, LOW); // Apagar el LED
}

//esto es solo para tener delays controlados
void retardos(){
  delay(100);
}