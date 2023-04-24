from abc import abstractmethod
from typing import Union

BYTEORDER ='little'

class Field:
    """Represents a field within a GATT characteristic"""
    def __init__(self, num_bytes: int) -> None:
        """
        Initializes the field with the given specification.
        :raises ValueError: If the length in bytes is smaller than or equal to 0
        """
        if num_bytes <= 0:
            raise ValueError(f'Length in bytes (={num_bytes}) must be a positive integer')
        self.num_bytes = num_bytes

    @abstractmethod
    def get_represented_value(self, raw_value: bytearray):
        pass

    @abstractmethod
    def get_raw_value(self, represented_value) -> bytearray:
        pass

class BooleanField(Field):
    """Represents a field within a GATT characteristic represinting a boolean value"""
    def __init__(self) -> None:
        """Initializes the field"""
        super().__init__(1)

    def get_represented_value(self, raw_value: Union[bytearray, bytes, int]) -> bool:
        if isinstance(raw_value, bytearray) or isinstance(raw_value, bytes):
            if len(raw_value) > 1:
                raise ValueError(f'Too many bytes ({len(bytearray)}) for boolean field')
            int_val = int.from_bytes(raw_value, BYTEORDER)
        else:
            int_val = raw_value
        if int_val == 0:
            return False
        elif int_val == 1:
            return True
        else:
            raise ValueError(f'Received value = {int_val} but only 0 or 1 is allowed')

    def get_raw_value(self, represented_value: bool) -> bytearray:
        return represented_value.to_bytes(1, BYTEORDER)

class BooleanArrayField(Field):
    """Represents a field within a GATT characteristic representing a boolean array"""
    def __init__(self, num_bytes: int) -> None:
        """
        Initializes the field with the given specification.
        :raises ValueError: If the length in bytes is smaller than or equal to 0
        """
        super().__init__(num_bytes)

    def get_represented_value(self, raw_value: bytearray) -> list[bool]:
        """
        Calculates the list of booleans value from the given bytearray.
        :param raw_value: Raw value as bytearray
        :raises ValueError: If there are more booleans in the bytearray than allowed
        """
        if len(raw_value) > self.num_bytes:
            raise ValueError(f'Too many bytes ({len(bytearray)}) for given field ({self.num_bytes} bytes allowed)')
        represented_value = []
        for byte in raw_value:
            for i in range(8):
                represented_value.append(bool((byte >> i) & 1))
        return represented_value

    def get_raw_value(self, represented_value: list[bool]) -> bytearray:
        """
        Calculates the raw value (as bytearray) from the given represented value.
        :param represented_value: Represented value as list of booleans
        :raises ValueError: If the represented value contains more values than allowed
        """
        if len(represented_value) > self.num_bytes * 8:
            raise ValueError(f'Too many values ({len(represented_value)}) for given field ({self.num_bytes * 8} allowed)')
        return sum(map(lambda x: x[1] << x[0], enumerate(represented_value))).to_bytes(self.num_bytes, BYTEORDER)


class ScalarField(Field):
    """Represents a field within a GATT characteristic representing a scalar value"""
    def __init__(self,
                 multiplier: int,
                 decimal_exponent: int,
                 binary_exponent: int,
                 num_bytes: int,
                 min: float = None,
                 max: float = None) -> None:
        """
        Initializes the field with the given specification.
        :param multiplier: Positive or negative multiplier between -10 and +10
        :param decimal_exponent: Positive or negative integer
        :param binary_exponent: Positive or negative integer
        :param min: Minimum allowed represented value
        :param max: Maximum allowed represented value
        :param num_bytes: Length of the raw value in bytes
        :raises ValueError: If the multiplier is < -10 or > +10
        :raises ValueError: If the minimum value is greater than or equal to the maximum value
        :raises ValueError: If the length in bytes is smaller than or equal to 0
        """
        super().__init__(num_bytes)

        # validate
        if not -10 <= multiplier <= +10:
            raise ValueError(f'Multiplier (={multiplier}) must be between -10 and +10')
        if min and max and min >= max:
            raise ValueError(f'Minimum value (={min}) must be smaller than maximum value (={max})')
        
        self.multiplier = multiplier
        self.decimal_exponent = decimal_exponent
        self.binary_exponent = binary_exponent
        self.min = min
        self.max = max
        self.signed = self.min != 0 and self.max != 0

    def get_represented_value(self, raw_value: bytearray) -> float:
        """
        Calculates the represented value from the given bytearray.
        :param raw_value: Raw value as bytearray
        :raises ValueError: If the calculated represented value is outside the allowed range
        """
        represented_value = int.from_bytes(raw_value, BYTEORDER, signed=self.signed) * self.multiplier * 10**self.decimal_exponent * 2**self.binary_exponent
        if (self.min and not self.min <= represented_value) or (self.max and not represented_value <= self.max):
            raise ValueError(f'Calculated value (={represented_value}) is outside allowed range [{self.min}, {self.max}]')
        return represented_value

    def get_raw_value(self, represented_value: float) -> bytearray:
        """
        Calculates the raw value (as bytearray) from the given represented value.
        :param represented_value: Represented value as float
        :raises ValueError: If the given represented value is outside the allowed range
        """
        if (self.min and not self.min <= represented_value) or (self.max and not represented_value <= self.max):
            raise ValueError(f'Given value (={represented_value}) is outside allowed range [{self.min}, {self.max}]')
        raw_int_value = int(represented_value / (self.multiplier * 10**self.decimal_exponent * 2**self.binary_exponent))
        return raw_int_value.to_bytes(self.num_bytes, BYTEORDER, signed=self.signed) 

class IndexField(Field):
    """Represents a field within a GATT characteristic representing an index"""
    def __init__(self, num_bytes: int) -> None:
        """
        Initializes the field with the given specification
        :param num_bytes: Length of the raw value in bytes
        :raises ValueError: If the length in bytes is smaller than or equal to 0
        """
        super().__init__(num_bytes)

    def get_represented_value(self, raw_value: bytearray) -> int:
        """
        Calculates the represented value from the given bytearray.
        :param raw_value: Raw value as bytearray
        :raises ValueError: If the calculated represented value is outside the allowed range
        """
        represented_value = int.from_bytes(raw_value, BYTEORDER)
        if not 0 <= represented_value < 2**(8 * self.num_bytes):
            raise ValueError(f'Calculated value (={represented_value}) is outside allowed range [0, {2**(8 * self.num_bytes)}]')
        return represented_value
    
    def get_raw_value(self, represented_value: int) -> bytearray:
        """
        Calculates the raw value from the given represented value.
        :param represented_value: Represented value
        :raises ValueError: If the given represented value is outside the allowed range
        """
        if not 0 <= represented_value < 2**(8 * self.num_bytes):
            raise ValueError(f'Given value (={represented_value}) is outside allowed range [0, {2**(8 * self.num_bytes)}]')
        return represented_value.to_bytes(self.num_bytes, BYTEORDER)