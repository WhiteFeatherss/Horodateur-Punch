package DatabaseAccess;

import javax.management.DescriptorAccess;
import javax.swing.plaf.synth.Region;
import javax.swing.text.html.HTMLDocument;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import java.io.IOException;
import java.nio.file.Path;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class myDBAccess
{
    /* CONSTANTS */
    private static String DB_CONNECTOR = "jdbc:sqlite:";
    private static String DB_LOCATION = "C:/SQLITE/DB/";
    private static String DB_Name = "Horodateur.db";


    private static String DB_URL = DB_CONNECTOR + DB_LOCATION + DB_Name;

    private static Connection connection = null;
    private static Statement statement = null;
    private static PreparedStatement p_statement = null;


    public static void main(String[] args)
    {

        DeleteDB("Horodateur.db");
        CreateDB(DB_Name);
        DropTable("employee");
        CreateTables();
        /* no */

    }

    private static void Populated_Department()
    {
        Insert_Department("Deli");
        Insert_Department("Butcher");
        Insert_Department("Dairy");
        Insert_Department("Bakery");
        Insert_Department("Packaged");
        Insert_Department("CheckOut");
    }

    private static void Insert_Department(String DepartmentName)
    {
        String SQL_Insert = "INSERT INTO DEPARTMENTS (Department_Name) VALUES (?)";

        try
        {
            OpenDB(DB_URL);

            p_statement = connection.prepareStatement(SQL_Insert);

            p_statement.setString(1, DepartmentName);

            p_statement.executeUpdate();

            p_statement.close();
            CloseDB();
        }
        catch(SQLException e)
        {
            printSQLException(e);
        }
    }

    //region Create and Drop Tables
    private static void DropTable(String tableToDrop)
    {
        String SQL = "DROP TABLE IF EXISTS " + tableToDrop;

        try
        {
            OpenDB(DB_URL);

            statement.executeUpdate(SQL);

            System.out.println("Table named " + tableToDrop + " has been fully deleted.");

            CloseDB();
        }
        catch(SQLException e)
        {
            printSQLException(e);
        }
    }
    private static void CreateTables()
    {

        String DEPARTMENTS =
                "CREATE TABLE IF NOT EXISTS DEPARTMENTS (\n"
                        + "    Department_ID   INTEGER CONSTRAINT DEPARTMENT_DEPARTMENT_ID_PK   PRIMARY KEY AUTOINCREMENT ,\n"
                        + "    Department_Name TEXT    CONSTRAINT DEPARTMENT_DEPARTMENT_NAME_NN NOT NULL UNIQUE"
                        + ");";

        String EMPLOYEES =
                "CREATE TABLE IF NOT EXISTS EMPLOYEES (\n"
                        + "    Employee_ID        INTEGER  CONSTRAINT EMPLOYEE_EMPLOYEE_ID_PK PRIMARY KEY AUTOINCREMENT ,\n"
                        + "    Employee_FirstName TEXT     CONSTRAINT EMPLOYEE_EMPLOYEE_FIRST_NAME_NN NOT NULL ,\n"
                        + "    Employee_LastName  TEXT     CONSTRAINT EMPLOYEE_EMPLOYEE_LAST_NAME_NN NOT NULL ,\n"
                        + "    Department_ID      INTEGER  CONSTRAINT EMPLOYEE_DEPARTMENT_ID_FK REFERENCES DEPARTMENTS(Department_ID),\n"
                        + "    Joined_In          DATETIME DEFAULT    CURRENT_TIMESTAMP"
                        + ");";

        String PUNCH =
                "CREATE TABLE IF NOT EXISTS PUNCH (\n"
                        + "    Employee_ID INTEGER  CONSTRAINT PUNCH_EMPLOYEE_ID_FK REFERENCES EMPLOYEES(Employee_ID),\n"
                        + "    Punch_IN    DATETIME DEFAULT   CURRENT_TIMESTAMP"
                        + ");";

        try
        {
            OpenDB(DB_URL);

            statement.executeUpdate(DEPARTMENTS);
            statement.executeUpdate(EMPLOYEES);
            statement.executeUpdate(PUNCH);

            CloseDB();
        }
        catch(SQLException e)
        {
            //System.out.print(e);
            printSQLException(e);
        }
    }
    //endregion

    //region Create and Delete DB
    private static void CreateDB(String fileName)
    {
        String url = DB_CONNECTOR + DB_LOCATION + fileName;

        try
        {
            CreatePath();

            if(!CheckDB_Exist(fileName))
            {
                connection = DriverManager.getConnection(url);

                if (connection != null)
                {
                    DatabaseMetaData meta = connection.getMetaData();

                    System.out.println("The driver name is " + meta.getDriverName());
                    System.out.println("A new database named " + DB_Name + " been created.");
                }
            }
            else
            {
                System.out.println("This database name is already in use.");
            }

        }
        catch (SQLException e)
        {
            printSQLException(e);
        }
    }
    private static void DeleteDB(String fileName)
    {
        File file = new File(DB_LOCATION + fileName);

        try
        {
            if(file.delete())
            {
                System.out.println(fileName + " has been deleted.");
            }
            else
            {
                System.out.println("The " + fileName + " file has it is, cannot be delete, check if it is not in use somewhere else.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
    //endregion

    //region Check up whether the DB or Path already exists.
    private static boolean CheckDB_Exist(String newDB_Name)
    {
        File file = new File(DB_LOCATION + newDB_Name);

        return file.exists();
    }
    private static void CreatePath()
    {
        Path path = Paths.get(DB_LOCATION);

        if (!Files.exists(path))
        {
            try
            {
                Files.createDirectories(path);
                System.out.println("The path did not exist prior, it has been created at : " + DB_LOCATION);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Path to the Database already Exists... continuing.");
        }
    }
    //endregion

    //region Open and Closes the Database
    private static void OpenDB(String inputURL)
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException nfE)
        {
            System.out.println(nfE);
        }
        try
        {
            connection = DriverManager.getConnection(inputURL);
            statement = connection.createStatement();
        }
        catch(SQLException e)
        {
            printSQLException(e);
        }
    }
    private static void CloseDB()
    {
        try
        {
            connection.close();
            statement.close();
        }
        catch(SQLException e)
        {
            printSQLException(e);
        }
    }
    //endregion

    //region Ignore and Print SQL Exceptions
    private static void printSQLException(SQLException ex)
    {

        for (Throwable ThrowEx : ex)
        {
            if (ThrowEx instanceof SQLException)
            {
                if (!ignoreSQLException(((SQLException) ThrowEx).getSQLState()))
                {
                    ThrowEx.printStackTrace(System.err);

                    System.err.println("SQLState: " +((SQLException)ThrowEx).getSQLState());
                    System.err.println("Error Code: " +((SQLException)ThrowEx).getErrorCode());
                    System.err.println("Message: " + ThrowEx.getMessage());

                    Throwable throwable = ex.getCause();

                    while(throwable != null)
                    {
                        System.out.println("Cause: " + throwable);

                        throwable = throwable.getCause();
                    }
                }
            }
        }
    }
    private static boolean ignoreSQLException(String sqlState)
    {
        if (sqlState == null)
        {
            System.out.println("The SQL state is not defined!");
            return false;
        }

        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
        {
            return true;
        }

        // 42Y55: Table already exists in schema
        return sqlState.equalsIgnoreCase("42Y55");
    }
    //endregion
}
