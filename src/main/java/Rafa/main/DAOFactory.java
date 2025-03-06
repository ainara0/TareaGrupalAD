package Rafa.main;

import Ainara.db4o.Db4oDAO;
import Ainara.mongoDB.MongoDBDAO;
import DAO.IDAO;
import Pere.file.TextFileDAO;
import Pere.jdbc.PostgreSQLDAO;
import Rafa.hibernate.HibernateDAO;

public class DAOFactory {
    public static IDAO getDAO(int option) {
        switch (option) {
            case 1:
                return new HibernateDAO();
            case 2:
                return new Db4oDAO(); // Implementación de Db4o
            case 3:
                return new MongoDBDAO(); // Implementación de MongoDB
            case 4:
                return new PostgreSQLDAO(); // Implementación de JDBC
            case 5:
                return new TextFileDAO();
            case 0:
                return null;
            default:
                throw new IllegalArgumentException("Opción no válida");
        }
    }
}
