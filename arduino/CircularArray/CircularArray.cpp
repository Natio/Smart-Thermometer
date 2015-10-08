
#include "Arduino.h"
#include "CircularArray.h"
#define CIRCULAR_ARRAY_SIZE 20

CircularArray::CircularArray(int size){
  _buffer = (float *)malloc(sizeof(float) * size);
  _buffer_size = size;
  _current_size = 0;
  _circular_index = 0;
}

void CircularArray::add(float f){
  if(_current_size < _buffer_size){
    _current_size++;
  }
  _buffer[_circular_index] = f;
  _circular_index = (_circular_index + 1) % _buffer_size;
}

float CircularArray::average(){
  if(_current_size == 0){
    return INVALID_AVG_VAL;
  }
  float tot = 0.0;
  for(int i = 0; i < _current_size; i++){
    tot += _buffer[i];
  }
  return tot/ (float)_current_size;
}
