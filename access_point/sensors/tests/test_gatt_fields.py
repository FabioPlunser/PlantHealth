import pytest
import random

from ..gatt_fields import BooleanArrayField, ScalarField

def test_boolean_field_basic_decoding():
    """Basic boolean fields are correctly decoded"""
    original_value = 0x03.to_bytes(1,'big')
    field = BooleanArrayField(1)
    decoded = field.get_represented_value(original_value)
    assert decoded[0] == True
    assert decoded[1] == True
    for i in range(2,8):
        assert decoded[i] == False

def test_boolean_field_encoding_and_decoding():
    """Boolean fields are correctly encoded and decoded"""
    original_list = [bool(i % 3) for i in range(16)]
    original_list = [(i < 2) for i in range(16)]
    field = BooleanArrayField(2)
    encoded = field.get_raw_value(original_list)
    decoded = field.get_represented_value(encoded)
    assert decoded == original_list

def test_scalar_field_encoding_and_decoding():
    """Scalar fields are correctly encoded and decoded"""
    random.seed(42)
    for i in range(50):
        for num_bytes in range(1,9):
            # setup
            multiplier=random.randint(-10, 10)
            if multiplier == 0:
                multiplier = 1
            decimal_exponent=random.randint(-3, 1)
            binary_exponent=random.randint(-3, 1)
            diff = 256**num_bytes
            signed = random.randint(0,1) == 1
            if not signed:
                min = 0
                max = (diff - 1) * multiplier * 10**decimal_exponent * 2**binary_exponent
            else:
                min = diff / 2 * (-1) * multiplier * 10**decimal_exponent * 2**binary_exponent
                max = ((diff / 2) - 1) * multiplier * 10**decimal_exponent * 2**binary_exponent
            if multiplier < 0:
                tmp = min
                min = max
                max = tmp
            field = ScalarField(multiplier, decimal_exponent, binary_exponent, num_bytes, min, max)
            random_int_min, random_int_max = signed * (-1) * 2**(8*num_bytes - 1), 2**(8*num_bytes - signed) - 1
            original_value = random.randint(random_int_min, random_int_max) * multiplier * 10**decimal_exponent * 2**binary_exponent
            encoded = field.get_raw_value(original_value)
            decoded = field.get_represented_value(encoded)
            assert decoded == pytest.approx(original_value, abs= 2 * abs(multiplier) * 10**decimal_exponent * 2**binary_exponent, rel=1e-2)
