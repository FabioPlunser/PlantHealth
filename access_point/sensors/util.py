def get_short_uuid(uuid: str) -> str:
    """
    Extracts the relevant 4 half-bytes from the 32 digit UUID to identify a characteristic type.
    """
    if len(uuid.replace('-','')) == 32:
        return uuid[4:8]
    else:
        return uuid