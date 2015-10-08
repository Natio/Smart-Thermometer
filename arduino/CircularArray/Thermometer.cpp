#include "Arduino.h"
#include "Thermometer.h"

Thermometer::Thermometer(int pin){
  _pin = pin;
}

float Thermometer::read(){
  return (5.0 * analogRead(_pin) * 100.0) / 1024.0;
}
