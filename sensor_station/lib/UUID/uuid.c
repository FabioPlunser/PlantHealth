
#include <uuid.h>

#define UUID_STRING_LENGTH 37

#define DEFAULT_UUID_VERSION 4
#define DEFAULT_UUID_VARIANT 8

#define LOWER_BITS(x) ((x)& 0xF)
#define HIGHER_BITS(x) (((x) >> 4) & 0xF)

void format_uuid(uuid_t uuid, char * string);
const char * format_uuid_static(uuid_t uuid); 
const char * format_uuid_allocated(uuid_t uuid); 

uint8_t parse_hex_number(char c);
uint8_t parse_uint8_t(char h, char l);
uuid_t parse_uuid(const char * uuid_string);

void format_uint8_t(char * string, uint8_t number);
char format_hex_number(uint8_t num);

static char STATIC_UUID_STRING[UUID_STRING_LENGTH];

uuid_t null_uuid() {
	return (uuid_t) {
		.version = DEFAULT_UUID_VERSION,
		.variant = DEFAULT_UUID_VARIANT,
		.bits = {0}
	};
}

uuid_t random_uuid() {
	// TODO: Replace with actual Call to random Function
	return (uuid_t) {
		.version = DEFAULT_UUID_VERSION,
		.variant = DEFAULT_UUID_VARIANT,
		.bits = {
			0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef,
			0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef,
		}
	};
}

// Format a Version 4 Variant 8 UUID 
void format_uuid(uuid_t uuid, char * string) {
	uint8_t i = 0, bit_idx = 0;
	
	string[8] = string[13] = string[18] = string[23] = '-';

	// TODO: Replace the Loops and Format Calls using Macros
	i = 0;
	while (i < 8) {
		format_uint8_t(string + i, uuid.bits[bit_idx++]);
		i += 2;
	}
	
	i = 9;
	while (i < 13) {
		format_uint8_t(string + i, uuid.bits[bit_idx++]);
		i += 2;
	}

	string[14] = format_hex_number(uuid.version);
	format_uint8_t(string + 15, uuid.bits[bit_idx++]); 
	string[17] = format_hex_number(HIGHER_BITS(uuid.bits[bit_idx]));

	string[19] = format_hex_number(uuid.variant);
	string[20] = format_hex_number(LOWER_BITS(uuid.bits[bit_idx++]));
	format_uint8_t(string + 21, uuid.bits[bit_idx++]); 

	i = 24;
	while (i < 36) {
		format_uint8_t(string + i, uuid.bits[bit_idx++]);
		i += 2;
	}

	string[36] = '\0';
}

const char * format_uuid_static(uuid_t uuid) {
	format_uuid(uuid, STATIC_UUID_STRING);
	return STATIC_UUID_STRING;
}

const char * format_uuid_allocated(uuid_t uuid) {
	char * uuid_string = (char *) malloc(UUID_STRING_LENGTH * sizeof(char));
	format_uuid(uuid, uuid_string);
	return uuid_string;
}

uuid_t parse_uuid(const char * uuid_string) {
	uuid_t uuid;

	uuid.version = parse_hex_number(uuid_string[14]); 
	uuid.variant = parse_hex_number(uuid_string[19]); 

	// TODO: Replace the Loops and Parse Calls using Macros
	uint8_t i = 0, bit_idx = 0;
	while (i < 8) {
		uuid.bits[bit_idx++] = parse_uint8_t(uuid_string[i], uuid_string[i + 1]);
		i += 2;
	}

	i = 9;
	while (i < 13) {
		uuid.bits[bit_idx++] = parse_uint8_t(uuid_string[i], uuid_string[i + 1]);
		i += 2;
	}

	uuid.bits[bit_idx++] = parse_uint8_t(uuid_string[15], uuid_string[16]);
	uuid.bits[bit_idx++] = parse_uint8_t(uuid_string[17], uuid_string[20]);
	uuid.bits[bit_idx++] = parse_uint8_t(uuid_string[21], uuid_string[22]);

	i = 24;
	while (i < 36) {
		uuid.bits[bit_idx++] = parse_uint8_t(uuid_string[i], uuid_string[i + 1]);
		i += 2;
	}
	return uuid;
}

uint8_t parse_uint8_t(char h, char l) {
	return (parse_hex_number(h) << 4) + parse_hex_number(l);
}

uint8_t parse_hex_number(char c) {
	if ((c >= '0') && (c <= '9')) {
		return c - '0';
	} else if ((c >= 'a') && (c <= 'f')) {
		return (c - 'a') + 10; 
	} else if ((c >= 'A') && (c <= 'F')) {
		return (c - 'A') + 10;
	} 
	return 0;
}

void format_uint8_t(char * string, uint8_t number) {
	string[0] = format_hex_number(HIGHER_BITS(number));
	string[1] = format_hex_number(LOWER_BITS(number));
}

char format_hex_number(uint8_t num) {
	static const char * characters = "0123456789abcdef";
	return characters[num & 0xF];
}
