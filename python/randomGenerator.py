import random
import string


# Generate a random name
def generate_name():
    names = [
        "Alice",
        "Bob",
        "Charlie",
        "David",
        "Emma",
        "Frank",
        "Grace",
        "Henry",
        "Isabella",
        "Jack",
        "Katherine",
        "Liam",
        "Mia",
        "Noah",
        "Olivia",
        "Peter",
        "Quinn",
        "Rachel",
        "Sophia",
        "Thomas",
        "Ursula",
        "Victor",
        "Wendy",
        "Xavier",
        "Yara",
        "Zoe",
    ]
    return random.choice(names)


# Generate a random password
def generate_password(length=8):
    characters = string.ascii_letters + string.digits + string.punctuation
    return "".join(random.choice(characters) for i in range(length))


# Generate a random email
def generate_email(name):
    domains = ["gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com"]
    domain = random.choice(domains)
    return f"{name.lower()}@{domain}"


def generate_room_name():
    words = [
        "apple",
        "banana",
        "cherry",
        "diamond",
        "elephant",
        "forest",
        "guitar",
        "helicopter",
        "island",
        "jungle",
        "kangaroo",
        "lion",
        "mountain",
        "ocean",
        "parachute",
        "quicksand",
        "river",
        "safari",
        "tiger",
        "unicorn",
        "volcano",
        "waterfall",
        "xylophone",
        "yacht",
        "zebra",
    ]
    room_name = random.choice(words) + "-" + random.choice(words)
    return room_name


def generate_ble_mac_address():
    # BLE MAC addresses start with "00:A0:50", followed by 3 pairs of random hex digits
    prefix = "00:A0:50"
    hex_chars = "0123456789ABCDEF"
    pairs = [random.choice(hex_chars) + random.choice(hex_chars) for _ in range(3)]
    mac_address = prefix + ":".join(pairs)
    return mac_address
