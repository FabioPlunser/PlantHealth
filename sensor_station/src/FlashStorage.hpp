#ifndef FLASH_STORAGE_CLASS
#define FLASH_STORAGE_CLASS

#include <Arduino.h>
#include <FlashIAPBlockDevice.h>
#include <cassert>
#include <string>
#include <vector>

// The arduino requires the flash buffer to be aligned to the block size
#define FLASH_BLOCK_SIZE 4096
// We need to align the buffer to the block size
#define FLASH_BUFFER_ALIGN(val)                                 \
	((((val) + ((FLASH_BLOCK_SIZE) -1)) / (FLASH_BLOCK_SIZE)) * \
	 (FLASH_BLOCK_SIZE))
// The size of the aligned buffer in bytes
#define FLASH_BUFFER_SIZE_ALIGNED FLASH_BUFFER_ALIGN(1024)
// The buffer itself, aligned to the block size
alignas(FLASH_BLOCK_SIZE
) const uint8_t flash_buffer[FLASH_BUFFER_SIZE_ALIGNED] = {};

class FlashStorage {
	private:
		// A struct for a storage block with a start address and size to make it
		// easier to read and write
		struct storageBlock {
				uint32_t startAdress;
				uint32_t sizeBytes;
		};
		// The total size of the flash buffer
		const uint32_t FLASH_BLOCK_START =
			reinterpret_cast<uint32_t>(flash_buffer);
		// The block to store the paired device in
		const struct storageBlock PAIRED_DEVICE_BLOCK = {
			FLASH_BLOCK_START, FLASH_BUFFER_ALIGN(64)};

		FlashIAPBlockDevice * flash;

		// Private constructor to make the class a singleton
		FlashStorage() {
			this->flash =
				new FlashIAPBlockDevice(FLASH_BLOCK_START, FLASH_BLOCK_SIZE);
			this->flash->init();
		}

		~FlashStorage() { delete this->flash; }

	public:
		// Delete copy and move constructors and assignment operators
		FlashStorage & operator=(FlashStorage &) = delete;
		FlashStorage(FlashStorage &)			 = delete;

		// Get the singleton instance of the class
		static FlashStorage * getInstance() {
			static FlashStorage storage;
			return &storage;
		}

		/**
		 * Store the paired device in flash
		 * @param deviceName: The data to store. Must be less than the size of
		 * the buffer
		 */
		void writePairedDevice(const arduino::String & deviceName) {
			assert(deviceName.length() < PAIRED_DEVICE_BLOCK.sizeBytes);
			this->flash->erase(
				PAIRED_DEVICE_BLOCK.startAdress, PAIRED_DEVICE_BLOCK.sizeBytes
			);
			uint8_t * ramBuffer = new uint8_t[PAIRED_DEVICE_BLOCK.sizeBytes];
			uint32_t lastI		= 0;
			for (uint32_t i = 0; i < deviceName.length(); i++) {
				ramBuffer[i] = deviceName[i];
				lastI		 = i;
			}
			arduino::String emptyString = "";
			emptyString.c_str();
			ramBuffer[lastI + 1] = '\0';
			this->flash->program(
				ramBuffer, PAIRED_DEVICE_BLOCK.startAdress,
				PAIRED_DEVICE_BLOCK.sizeBytes
			);
			delete[] ramBuffer;
		}

		/**
		 * Reads the paired device from flash. If no device is paired the
		 * behaviour is undefined.
		 */
		arduino::String readPairedDevice() {
			uint8_t * ramBuffer = new uint8_t[PAIRED_DEVICE_BLOCK.sizeBytes];
			this->flash->read(
				ramBuffer, PAIRED_DEVICE_BLOCK.startAdress,
				PAIRED_DEVICE_BLOCK.sizeBytes
			);
			arduino::String pairedDevice;
			for (uint32_t i = 0; i < PAIRED_DEVICE_BLOCK.sizeBytes; i++) {
				if (ramBuffer[i] == 0) {
					break;
				}
				pairedDevice += ramBuffer[i];
			}
			return pairedDevice;
		}

		uint32_t getMaxSize_PairedDevice() {
			return PAIRED_DEVICE_BLOCK.sizeBytes - 1;
		}
};

#endif // FLASH_STORAGE_CLASS