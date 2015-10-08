#ifndef Thermometer_h
#define Thermometer_h

#include "Arduino.h"

class Thermometer
{
  public:
    Thermometer(int pin);
    float read();
  private:
    int _pin;
};

#endif
