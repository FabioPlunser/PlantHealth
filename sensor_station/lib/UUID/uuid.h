
#ifndef UUID_H
#define UUID_H

#include <stdint.h>
#include <stdlib.h>

typedef struct {
	uint8_t version;
	uint8_t variant;
	uint8_t bits[16];
} uuid_t;

uuid_t null_uuid();
uuid_t random_uuid();

void format_uuid(uuid_t uuid, char * string);
const char * format_uuid_static(uuid_t uuid);
const char * format_uuid_allocated(uuid_t uuid);

uuid_t parse_uuid(const char * uuid_string);

#endif
