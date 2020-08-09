#include <SoftwareSerial.h>

#define pulsanteLedGiuseppe 4
#define pulsanteLedSimone 3
#define cellGiuseppe 6
#define cellSimone 5
#define ledGiuseppe 10  //sopra la testa di Giuseppe
#define ledSimone 9
#define bluetoothInterruttore 11
#define bluetooth 8
#define computer 7
#define thermistorpin A0
#define bcoefficient 3763.45
#define thermistornominal 2460
#define temperaturenominal 60
#define seriesresistor 21400

#define  BT_RX 12           // PIN to receive from bluetooth
#define  BT_TX 13            // PIN TO transmit to bluetooth
 
SoftwareSerial btSerial(BT_RX, BT_TX);
int i,j;
boolean firstTouchGiuseppe=true,firstTouchSimone=true;
long lastmillis=0,ultimomillis=-18000000;
float reading;
float gradi;  
byte val;


void setup() {
  // initialize serial communication at 9600 bits per second:
  Serial.begin(14400);
  btSerial.begin(9600); 
  pinMode(ledGiuseppe, OUTPUT);      
  pinMode(pulsanteLedSimone, INPUT);
  pinMode(ledSimone, OUTPUT);      
  pinMode(pulsanteLedGiuseppe, INPUT);
  pinMode(computer, OUTPUT);      
  pinMode(cellSimone, OUTPUT);      
  pinMode(cellGiuseppe, OUTPUT);
  digitalWrite(computer,HIGH);
  digitalWrite(cellSimone,HIGH);
  digitalWrite(cellGiuseppe,HIGH);

  pinMode(bluetoothInterruttore, INPUT);
  pinMode(bluetooth, OUTPUT);
}

// the loop routine runs over and over again forever:
void loop() {
  
  if(digitalRead(bluetoothInterruttore)==HIGH)
    digitalWrite(bluetooth, HIGH);

if(digitalRead(pulsanteLedGiuseppe)==HIGH || digitalRead(pulsanteLedSimone)==HIGH)
Pulsanti();

bluetoothFunction();

delay(30);
}




void Pulsanti(){
  int pulsGiuseppe = digitalRead(pulsanteLedGiuseppe);  // legge il valore dell'input e lo conserva
  int pulsSimone= digitalRead(pulsanteLedSimone);
  long currmills=millis();  
  
  
  
if((currmills-lastmillis)>200){
    if (pulsGiuseppe == HIGH && firstTouchGiuseppe==true) {
     delay(400);
     lastmillis=currmills;
    for(j=0;j<256;j++){
       if(digitalRead(pulsanteLedGiuseppe)==HIGH){
        delay(300);
        break;
       }
      analogWrite(ledSimone,j);
      delay(30);  //accende il led
    }
    firstTouchGiuseppe=false;
  }else if(digitalRead(pulsanteLedGiuseppe)==HIGH && firstTouchGiuseppe==false){
    lastmillis=currmills;
    for(;j!=0;j--){
    analogWrite(ledSimone,j);  
    delay(30);
    }
    digitalWrite(ledSimone,LOW);
   firstTouchGiuseppe=true;
  }
  
    if (pulsSimone == HIGH && firstTouchSimone==true) {
     delay(400);
     lastmillis=currmills;
    for(i=0;i<256;i++){
      
       if(digitalRead(pulsanteLedSimone)==HIGH){
        delay(300);
        break;
       }
      analogWrite(ledGiuseppe,i);
      delay(30);  //accende il led
    }
    firstTouchSimone=false;
  }else if(digitalRead(pulsanteLedSimone)==HIGH && firstTouchSimone==false){
    lastmillis=currmills;
    for(;i!=0;i--){
    analogWrite(ledGiuseppe,i);  
    delay(30);
    }
    digitalWrite(ledGiuseppe,LOW);
   firstTouchSimone=true;
  }
  
  

} 

}


void bluetoothFunction(){
  
  if(btSerial.available() > 0) {
    Serial.print(val);
   
    val = btSerial.read();
    int bright=25;
    int brightSim=25;
      int inpBuffer[4];
      char inpChar;
      byte Buffer;
      int inpBuffer2[4];
     byte w;
     int temp_=0;
    
    switch (val){
        case 'A':
            firstTouchSimone=false;
            btSerial.flush();
            
            do{
            Buffer = 0;
            
              while ( btSerial.available()>0 ) {
                
               inpChar = btSerial.read(); 
               if (inpChar == 'e') break;
               if(inpChar!='D'){
                 inpBuffer[Buffer] = (inpChar-'0');
                         Buffer++;
                 inpBuffer[Buffer] = 0x00;
                 analogWrite(ledGiuseppe,bright);
               }
               
               
                       delay(1);
                  }
                if(Buffer==3)
                  bright=inpBuffer[2]+inpBuffer[1]*10+inpBuffer[0]*100;
                if(Buffer==2)
                  bright=inpBuffer[1]+inpBuffer[0]*10;
                if(Buffer==1)
                   bright=inpBuffer[0];
                 analogWrite(ledGiuseppe,bright);
            }while(inpChar!='D');
            i=bright;
            break;
         case 'a':
           firstTouchSimone=true;
           i=abs(i);
           for(;i>=0;i--){
                analogWrite(ledGiuseppe,i);  
                delay(10);
            }
           digitalWrite(ledGiuseppe,LOW);
           break;  
        case 'B':
            firstTouchGiuseppe=false;
              btSerial.flush();
            
            do{
            Buffer = 0;
           
              while ( btSerial.available()>0 ) {
                
               inpChar = btSerial.read();
               if (inpChar == 'e') break;
               if(inpChar!='Q'){
                 inpBuffer2[Buffer] = (inpChar-'0');
                         Buffer++;
                 inpBuffer2[Buffer] = 0x00;
                 analogWrite(ledSimone,brightSim);
               }
               
               
                       delay(1);
                  }
                if(Buffer==3)
                  brightSim=inpBuffer2[2]+inpBuffer2[1]*10+inpBuffer2[0]*100;
                if(Buffer==2)
                  brightSim=inpBuffer2[1]+inpBuffer2[0]*10;
                if(Buffer==1)
                brightSim=inpBuffer2[0];
                 analogWrite(ledSimone,brightSim);
            }while(inpChar!='Q');
            
            
            j=brightSim;
            
            
            
            break;
         case 'b':
         firstTouchGiuseppe=true;
         j=abs(j);
           for(;j>=0;j--){
                analogWrite(ledSimone,j);  
                delay(10);
            }
           digitalWrite(ledSimone,LOW);
           break;  
        case 'T':
        for(w=0;w<10;w++){
            reading=analogRead(A0);
            reading=(1023/reading)-1;
            reading=seriesresistor/reading;
          gradi=reading/thermistornominal;
          gradi=log(gradi);
          gradi/=bcoefficient;
          gradi+=1.0/(temperaturenominal+273.15);            //sonda ntc
          gradi=1.0/gradi;
          gradi-=273.15;
          temp_+=gradi;
      }
          btSerial.print((int)gradi);
          btSerial.print(".");
          
          break;
        case 's':
          digitalWrite(bluetooth, LOW);
          break;  
          case 'y':
          digitalWrite(cellSimone,LOW);
          break;
           case 'Y':
          digitalWrite(cellSimone,HIGH);
          break;
           case 'z':
          digitalWrite(cellGiuseppe,LOW);
          break;
           case 'Z':
          digitalWrite(cellGiuseppe,HIGH);
          break;
           case 'w':
          digitalWrite(computer,LOW);
          break;
           case 'W':
          digitalWrite(computer,HIGH);
          break;
    }
  }
}






