#include <CircularArray.h>
#include <Thermometer.h>
#include <Dhcp.h>
#include <Dns.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include <EthernetUdp.h>
#include <SPI.h>
#include <SD.h>

// Thermometer Constants & Variables
#define INSIDE_ANALOG_PIN 5
#define OUTSIDE_ANALOG_PIN 0

CircularArray inside(20);
CircularArray outside(20);
Thermometer in_thermometer(INSIDE_ANALOG_PIN);
Thermometer out_thermometer(OUTSIDE_ANALOG_PIN);

// Networking Constants & Variables
char* server_address="smartthermometer.eu-gb.mybluemix.net";
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDE, 0x08 };
IPAddress ip(192, 168, 0, 178);
IPAddress myDns(192,168,0, 1);
IPAddress gateway(192, 168, 0, 1);
IPAddress subnet(255, 255, 255, 0);
EthernetClient client;

// Files
File bufferFile;


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  while(!Serial){}

  // start the Ethernet connection:
  Ethernet.begin(mac, ip, myDns, gateway, subnet);  
  //Ethernet.begin(mac);// use the latter to use DHCP
  Serial.print("My IP address: ");
  ip = Ethernet.localIP();
  for (byte thisByte = 0; thisByte < 4; thisByte++) {
    // print the value of each byte of the IP address:
    Serial.print(ip[thisByte], DEC);
    Serial.print(".");
  }


  Serial.print("Initializing SD card...");

  if (!SD.begin(4)) {
    Serial.println("initialization failed!");
    return;
  }
  Serial.println("initialization done.");
  
  Serial.println();
  delay(1000);
}

int loop_count = 1;

void loop() {
  
  inside.add(in_thermometer.read());
  outside.add(out_thermometer.read());

  delay(1000);
  
  if(loop_count >= 60 * 5){ //send temperature every ~5 min
    float in = inside.average();
    float out = outside.average();
    sendTemperature(in, out);
    Serial.print(in);
    Serial.print(" ");
    Serial.println(out);
    loop_count = 0;
  }
  loop_count++;
}

void sendTemperature(float inside, float outside){
  Serial.println("connecting...");
  int c_res = client.connect(server_address, 80); 
  if (c_res) {
    Serial.println("connected");
    // Make a HTTP request:
    
    client.print("GET /?in=");
    client.print(inside);
    client.print("&out=");
    client.print(outside);
    client.println(" HTTP/1.1");
    client.println("Host: smartthermometer.eu-gb.mybluemix.net");
    client.println("Connection: close");
    client.println();
    Serial.println("close");
  } else {
    // kf you didn't get a connection to the server:
    Serial.println("Error code ");
    Serial.println(c_res);
    Serial.println("connection failed");
    bufferFile = SD.open("buffer.txt", FILE_WRITE);
    if(bufferFile){
      bufferFile.print(inside);
      bufferFile.print(",");
      bufferFile.print(outside);
      bufferFile.print(",");
      bufferFile.print(c_res);
      bufferFile.println();
      bufferFile.close();
    }
    return;
  }

  while(1){
    if (client.available()) {
      char c = client.read();
      Serial.print(c);
    }

    if (!client.connected()) {
      Serial.println();
      Serial.println("disconnecting.");
      client.stop();
      return;
    }
  }
  
}
