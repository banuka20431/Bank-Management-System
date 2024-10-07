import json
import sys


class JsonDataHandler: 
    def __init__(self, JSON_FILE_PATH):
        self.JSON_FILE_PATH = JSON_FILE_PATH
        self.JSON_DATA = self.getJsonData()

    def getJsonData(self) -> dict | bool:
        print("Reading json data..: ", end='')
        try:
            with open(self.JSON_FILE_PATH, "r") as info_json_file:
                json_data = json.load(info_json_file)
                if not json_data:
                    print("FAILED")
                    print(f"(!) {self.JSON_FILE_PATH} is empty")
                else:
                    print('OK')
                    return json_data
        except IOError as e:
            print("FAILED")
            print("(!) I/O error({0}): {1}".format(e.errno, e.strerror))
        except:
            print("FAILED")
            print("(!) Unexpected error:", sys.exc_info()[0])
        return False

    def writeJsonData(self, data, json_key):
        self.JSON_DATA[json_key] = data
        print("Writing to json data file..: ", end='')
        try:
            with open(self.JSON_FILE_PATH, "+w") as json_file:
                json_file.flush()
                json.dump(self.JSON_DATA, json_file, indent=2)
                print('OK')
                print(f"(!) Key '{json_key}' got updated!")
        except IOError as e:
            print("FAILED")
            print("(!) I/O error({}): {} Could not update '{}' to {}".format(e.errno, e.strerror, key, data))
        except:
            print("FAILED")
            print("(!) Unexpected error:", sys.exc_info()[0])
