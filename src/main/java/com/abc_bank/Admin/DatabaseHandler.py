from mysql import connector as sqlc
from mysql.connector import errorcode
import time


class DatabaseHandler:

    def __init__(self, DB_STRUCTURE_QUERY_FILE_PATH: str, db_login_info: dict, JSON_FILE_PATH: str):
        super().__init__(JSON_FILE_PATH)
        self.db_login_info = db_login_info
        self.DB_STRUCTURE_QUERY_FILE_PATH = DB_STRUCTURE_QUERY_FILE_PATH

    def init_connect(self, attempts=3, delay=2) -> object | None:
        login_credit = {
            'host': self.db_login_info["SERVER"],
            'user': self.db_login_info["USERNAME"],
            'passwd': self.db_login_info["PASSWORD"]
        }
        attempt = 1
        while attempt < attempts + 1:
            try:
                print("Connecting to the database server..: ", end='')
                new_conn = sqlc.connect(**login_credit)
                print("CONNECTED")
                return new_conn
            except sqlc.Error as err:
                if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                    print("FAILED")
                    print("(!) Something is wrong with your user name or password")
                    exit(0)
                else:
                    if attempt == attempts:
                        print("exiting without a connection")
                    else:
                        print("FAILED")
                        print(err, "\nRetrying ({}/{})...".format(attempt + 1, attempts))
                        attempt += 1
                        time.sleep(delay)
                        continue
            return None

    def construct_database(self, sql_conn: object) -> bool:
        sql_cursor = sql_conn.cursor()

        try:
            print("Reading query file..: ", end='')
            query = ""
            with open(self.DB_STRUCTURE_QUERY_FILE_PATH, "r") as query_file:
                for line in query_file.readlines():
                    query += line
        except FileNotFoundError:
            print('FAILED')
            print("\t(!) query file does not exists")
            return False
        else:
            print('OK')

        try:
            print("Executing construction queries..: ", end='')
            sql_cursor.execute(query, multi=True)
        except sqlc.Error as err:
            print('FAILED')
            print("\t(!) {}".format(err))
            return False
        else:
            print('OK')
            sql_cursor.close()
            return True

    def is_database_exist(self, sql_conn: object) -> bool:
        try:
            sql_conn.cursor().execute("USE {}".format(self.db_login_info["DATABASE NAME"]))
            return True
        except sqlc.Error as err:
            if err.errno == errorcode.ER_BAD_DB_ERROR:
                print("Database does not exist!")
            else:
                print("Error occurred {}".format(err))
            return False

    def get_new_db_conn(self):
        login_credit = {
            'host': self.db_login_info["SERVER"],
            'user': self.db_login_info["USERNAME"],
            'passwd': self.db_login_info["PASSWORD"],
            'database': self.db_login_info["DATABASE NAME"]
        }
        new_db_conn = sqlc.connect(**login_credit)
        return new_db_conn

    def insert_data(self, query, data):
        db_conn = self.get_new_db_conn()
        db_conn.raise_on_warnings = True
        try:
            db_conn.cursor().execute(query, data)
            print("\nNew employee added successfully..!\n")
            db_conn.commit()
        except sqlc.Error as err:
            print(f"\nInsertion failed..!\n{err}")
        finally:
            db_conn.close()


