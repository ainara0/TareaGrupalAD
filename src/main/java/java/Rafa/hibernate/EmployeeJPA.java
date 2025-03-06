package java.Rafa.hibernate;

import DAO.Employee;
import Utils.ConvertersJPA;

import jakarta.persistence.*;

@Entity
@Table(name = "empleado")
public class EmployeeJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empno", nullable = false)
    private Integer id;

    @Column(name = "nombre", length = 10)
    private String name;

    @Column(name = "puesto", length = 15)
    private String job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depno")
    private DepartmentJPA depno;

    public EmployeeJPA(String name, String job, DepartmentJPA depno) {
        this.name = name;
        this.job = job;
        this.depno = depno;
    }

    public EmployeeJPA() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public DepartmentJPA getDepno() {
        return depno;
    }

    public void setDepno(DepartmentJPA depno) {
        this.depno = depno;
    }

    @Override
    public String toString() {
        return "Employee{" + "id=" + id + ", name=" + name + ", job=" + job + "depno=" + depno + '}';
    }

}
