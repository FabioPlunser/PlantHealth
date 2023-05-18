
#ifndef STORAGE_H
#define STORAGE_H

#include <uuid.h>

#include <stdbool.h>

typedef struct {
		uuid_t uuid;
		bool initialized;
} arduino_info_t;

bool is_initialized();
bool initialize(uuid_t);

#endif
