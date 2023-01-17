package com.ma.uninstallBlack.util;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Entity(tableName = "person")
public class Person {

    /**
     * 使用@PrimaryKey声明为主键，并且允许自动生成
     * 使用@ColumnInfo表明这个属性是表中的一列列名，并可以指明列的名称
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    @ColumnInfo(name = "age")
    private int age;

    @ColumnInfo(name = "region")
    private String region;


    //必须指定一个构造方法，room框架需要。并且只能指定一个
    //如果有其他构造方法，则其他的构造方法必须添加@Ignore注解
    @Ignore
    public Person() {

    }


    /**
     * 为了更新数据时使用
     *
     * @param id
     * @param firstName
     */
    public Person(int id, String firstName) {
        this.id = id;
        this.firstName = firstName;
    }


    /**
     * 为了删除对象时使用
     *
     * @param id
     */
    @Ignore
    public Person(int id) {
        this.id = id;
    }

    /**
     * 为了方便直接构造对象使用
     *
     * @param firstName
     * @param lastName
     * @param age
     * @param region
     */
    @Ignore
    public Person(String firstName, String lastName, int age, String region) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.region = region;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getFirstName() {
        return firstName;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public int getAge() {
        return age;
    }


    public void setAge(int age) {
        this.age = age;
    }


    public String getRegion() {
        return region;
    }


    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", region='" + region + '\'' +
                '}';
    }


    /**
     * DAO层接口,需要添加@Dao注解声明
     * 所有的操作都以主键为依托进行
     */
    @Dao
    public interface PersonDao {

        /**
         * 查询所有的数据，返回List集合
         * @return
         */
        @Query("Select * from person")
        List<Person> getAllPersonList();
        /**
         * 传递参数的集合，注意 Room通过参数名称进行匹配，若不匹配，则编译出现错误
         * @param personId
         * @return
         */
        @Query("select * from person where id in (:personId)")
        List<Person> getPersonById(int[] personId);

        /**
         * 返回一定条件约束下的数据，注意参数在查询语句中的写法
         * @param minAge
         * @param maxAge
         * @return
         */
        @Query("select * from person where age between :minAge and :maxAge")
        List<Person> getPersonByChosen(int minAge, int maxAge);

        /**
         * 插入数据，onConflict = OnConflictStrategy.REPLACE表明若存在主键相同的情况则直接覆盖
         * 返回的long表示的是插入项新的id
         * @param person
         * @return
         */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long insertPerson(Person person);
        /**
         * 更新数据，这意味着可以指定id然后传入新的person对象进行更新
         * 返回的long表示更新的行数
         * @param person
         * @return
         */
        @Update
        int updatePerson(Person person);
        /**
         * 删除数据，根据传入实体的主键进行数据的删除。
         * 也可以返回long型数据，表明从数据库中删除的行数
         * @param person
         * @return
         */
        @Delete
        int deletePerson(Person person);
    }
}
