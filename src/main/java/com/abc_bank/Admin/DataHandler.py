from JsonDataHandler import JsonDataHandler
from DatabaseHandler import DatabaseHandler
import re
from hashlib import sha224


class DataHandler(DatabaseHandler, JsonDataHandler):

    def __init__(self, DB_STRUCTURE_QUERY_FILE_PATH: str, login_info: dict, JSON_FILE_PATH: str):
        super().__init__(DB_STRUCTURE_QUERY_FILE_PATH, login_info, JSON_FILE_PATH)

    @staticmethod
    def displayMenu(menuItems: tuple) -> int:
        while True:
            for item in menuItems:
                print(f"{menuItems.index(item) + 1}. {item}")
            print("\n >_ ", end="")
            try:
                optionId = int(input()) - 1
                if optionId >= len(menuItems):
                    print("\n~ Error: Incorrect Selection!\n")
                else:
                    return optionId
            except ValueError:
                print("\n~ Error: Invalid Input!!\n")

    @staticmethod
    def searchItem(itemList: tuple, reqItem: str) -> str | bool:
        for item in itemList:
            if reqItem.upper() == item.upper():
                return item
        return False

    def getEmpBranch(self) -> str | bool:
        branches = self.JSON_DATA["branches"]
        print("\nEnter Branch Name : ", end="")
        return self.searchItem(tuple(branches.keys()), input().strip())

    def getEmpDepartment(self) -> str | bool:
        departments = self.JSON_DATA["departments"]
        print("\nEnter Department Name : ", end="")
        return self.searchItem(tuple(departments.keys()), input().strip())

    @staticmethod
    def getEmpFullName() -> str | bool:
        empFullNameRegex = re.compile(r"^([a-zA-Z]+\s?)+")
        Name = input("\nEnter Employee Full Name : ").strip()
        if empFullNameRegex.fullmatch(Name) is not None:
            return Name
        else:
            return False

    def getEmpTitle(self) -> str | bool:
        titles = ("Mr", "Ms", "Miss", "Rev")
        print("\nSelect the preferred title for the name :\n")
        selectedOptionIndex = self.displayMenu(titles)
        if not -1 < selectedOptionIndex < len(titles):
            return False
        else:
            return titles[selectedOptionIndex]

    @staticmethod
    def getEmpLoginInfo() -> list[str, str]:
        empUsername = input("\nEnter Username for the account :: ").strip()
        empPassword = input("Enter Password for the account :: ").strip()
        return [empUsername, empPassword]

    def getEmployeeInfo(self) -> dict:
        empInfo = {}
        out = False
        empInfoFields = ("Full Name", "Preferred Title", "Branch", "Department", "Login Info")

        for infoField in empInfoFields:
            while True:
                match infoField:
                    case "Full Name":
                        out = self.getEmpFullName()
                    case "Preferred Title":
                        out = self.getEmpTitle()
                    case "Branch":
                        out = self.getEmpBranch()
                    case "Department":
                        out = self.getEmpDepartment()
                    case "Login Info":
                        out = self.getEmpLoginInfo()
                if out:
                    if infoField == "Login Info":
                        hash_method = sha224()
                        hash_method.update(out[1].encode("utf-8"))
                        out[1] = hash_method.hexdigest()
                    empInfo[infoField] = out
                    break
                else:
                    print(f"\n~ Error: Invalid Input\n\t(!) For the employee data field ' {infoField} '")
        return empInfo

    def addNewEmployee(self, emp_info: dict):
        emp_no = self.JSON_DATA["lastGivenEmployeeNumber"] + 1
        emp_branch_id = self.JSON_DATA["branches"][emp_info["Branch"]]
        emp_dep_id = self.JSON_DATA["departments"][emp_info["Department"]]
        emp_id = f"{emp_branch_id}-{emp_dep_id}-{emp_no}"
        query = "INSERT INTO Employee(empId, empName, empUserName, empPasswordHash, depId, branchId, empTitle) VALUES(%s , %s, %s, %s, %s, %s, %s)"
        data = (
            emp_id, emp_info["Full Name"],
            emp_info["Login Info"][0], emp_info["Login Info"][1],
            emp_dep_id,
            emp_branch_id,
            emp_info["Preferred Title"]
        )

        self.insert_data(query, data)
        self.writeJsonData(emp_no, "lastGivenEmployeeNumber")


