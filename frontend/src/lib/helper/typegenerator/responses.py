import sys
import yaml


def get_type(prop_details):
    prop_type = prop_details.get("type")
    prop_format = prop_details.get("format", "")
    prop_ref = prop_details.get("$ref", "")

    if prop_type == "integer" or prop_type == "number":
        return "number"
    elif prop_type == "array" and "$ref" in prop_details.get("items", {}):
        return f'{prop_details["items"]["$ref"].split("/")[-1]}[]'
    elif prop_type == "array":
        return "any[]"
    elif prop_format == "uuid":
        return "string"
    elif prop_ref:
        return prop_ref.split("/")[-1]

    return prop_type


def generate_ts_definition(yaml_file):
    with open(yaml_file, "r") as file:
        data = yaml.safe_load(file)

    schemas = data.get("components", {}).get("schemas", {})

    with open("responses.d.ts", "w") as file:
        file.write("declare namespace Responses {\n")

        for schema_name, schema_properties in schemas.items():
            file.write(f"  export interface {schema_name} " + "{\n")

            for prop_name, prop_details in schema_properties.get(
                "properties", {}
            ).items():
                prop_type = get_type(prop_details)
                if prop_format := prop_details.get("format", ""):
                    file.write(f"    /** Format: {prop_format} */\n")
                file.write(f"    {prop_name}: {prop_type};\n")

            file.write("  }\n\n")

        file.write("}")

    print(
        """TypeScript definition file "responses.d.ts"
     generated successfully."""
    )


if len(sys.argv) < 2:
    print("Please provide the YAML file as a command-line argument.")
    print("Usage: python yaml_to_ts.py input.yaml")
else:
    yaml_file = sys.argv[1]
    generate_ts_definition(yaml_file)
