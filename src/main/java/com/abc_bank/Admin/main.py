from DataHandler import DataHandler


def getDatabaseLoginCredit() -> dict:
    print("\n\t\t--- Set database login credentials ---\n")
    login_info = {}
    for dataField in ("SERVER", "USERNAME", "PASSWORD", "PORT", "DATABASE NAME"):
        login_info[dataField] = input(f"~# {dataField} : ")
    if login_info["PORT"] == '' or login_info["SERVER"] == '':
        login_info["SERVER"] = "localhost"
        login_info["PORT"] = "3306"
    if login_info["DATABASE NAME"] == '':
        login_info["DATABASE NAME"] = "ABC_BANK"
    print()
    return login_info


def main() -> None:
    if actions[selectedOptionId] == actions[0]:
        employee_info = data_handler.getEmployeeInfo()
        data_handler.addNewEmployee(employee_info)


if __name__ == "__main__":
    try:
        DB_STRUCTURE_QUERY_FILE_PATH = r"../db/structure.sql"
        INFO_JSON_FILE_PATH = r"src/main/java/com/abc_bank/Admin/info.json"
        db_login_credentials = getDatabaseLoginCredit()
        data_handler = DataHandler(DB_STRUCTURE_QUERY_FILE_PATH, db_login_credentials, INFO_JSON_FILE_PATH)
        JSON_DATA = data_handler.JSON_DATA
        if(JSON_DATA):
            db_connection = data_handler.init_connect(10)
            data_handler.writeJsonData(db_login_credentials, "dbLoginInfo")

            if not (database_exists := data_handler.is_database_exist(db_connection)):
                print("Constructing a new database...")
                if data_handler.construct_database(db_connection):
                    print("Resetting the json logs...")
                    data_handler.writeJsonData(9999, "lastGivenEmployeeNumber")
                    data_handler.writeJsonData(False, "IsResetMonthlyTransactionLimitDone")
                    database_exists = True

            if JSON_DATA and db_connection is not None and database_exists:
                db_connection.close()
                version = "v" + JSON_DATA["version"]
                print(
                    f"""
                                ===============================================================
                                +----------------- ABC BANK ADMIN PANEL {version} -------------+
                                ===============================================================
                    """
                )

                main_loop = True
                while main_loop:
                    actions = ("Add Employee", "Add Account Type")
                    print("\nSelect an Action > \n")
                    selectedOptionId = DataHandler.displayMenu(actions)
                    main()

                    main_loop = input("\nExit (Y/N) : ").strip().split()[0] in ('n', 'N')
    except KeyboardInterrupt:
        print("\n\nExiting...\n")
        exit(0)