#ifndef CircularArray_h
#define CircularArray_h

#include "Arduino.h"

#define INVALID_AVG_VAL -273.0;

class CircularArray
{
  public:
    CircularArray(int size);
    void add(float f);
    float average();
  private:
    float * _buffer;
    int _buffer_size;
    int _current_size;
    int _circular_index;
};

#endif
