package users;

import org.example.dao.UserDao;
import org.example.models.User;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTest {

    private static UserDao dao;

    @BeforeAll
    public static void setup() {
        dao = new UserDao();
        System.out.println("UserDao initialized");
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("All tests executed successfully");
    }

    @Test
    @Order(1)
    public void testAddUser() {
        User user = new User(201, "JUnitUser", 25, "test@123");

        User savedUser = null;
        try {
            savedUser = dao.addUser(user);
            Assertions.assertNotNull(savedUser);
            Assertions.assertEquals("JUnitUser", savedUser.getUsername());
        } finally {
            dao.updateAge(0, user.getUserId());
            deleteUser(user.getUserId());
        }
    }

    @Test
    @Order(2)
    public void testGetAllUsers() {
        User user = new User(202, "TempUser", 22, "temp@123");
        dao.addUser(user);

        try {
            List<User> users = dao.getAllUsers();
            Assertions.assertNotNull(users);
            Assertions.assertTrue(users.size() > 0);
        } finally {
            // CLEANUP
            deleteUser(user.getUserId());
        }
    }

    @Test
    @Order(3)
    public void testUpdateUserAge() {
        User user = new User(203, "UpdateUser", 20, "update@123");
        dao.addUser(user);

        try {
            User updatedUser = dao.updateAge(30, user.getUserId());
            Assertions.assertNotNull(updatedUser);
            Assertions.assertEquals(30, updatedUser.getAge());
        } finally {
            // CLEANUP
            deleteUser(user.getUserId());
        }
    }

    @Test
    @Order(4)
    public void testGetUserById() {
        User user = new User(204, "FetchUser", 28, "fetch@123");
        dao.addUser(user);

        try {
            User fetchedUser = dao.getUserById(user.getUserId());
            Assertions.assertNotNull(fetchedUser);
            Assertions.assertEquals("FetchUser", fetchedUser.getUsername());
        } finally {
            // CLEANUP
            deleteUser(user.getUserId());
        }
    }

    @Test
    @Order(5)
    public void testGetOldestUserAge() {
        User user1 = new User(205, "OldUser1", 40, "old1@123");
        User user2 = new User(206, "OldUser2", 35, "old2@123");
        dao.addUser(user1);
        dao.addUser(user2);

        try {
            Integer age = dao.getOldestUserAge();
            Assertions.assertNotNull(age);
            Assertions.assertTrue(age >= 40); // since we added 40
            System.out.println("Oldest age: " + age);
        } finally {
            // CLEANUP
            deleteUser(user1.getUserId());
            deleteUser(user2.getUserId());
        }
    }

    private void deleteUser(int userId) {
        try {
            String sql = "DELETE FROM users WHERE user_id = ?";
            dao.getClass()
                    .getDeclaredField("conn") // access private conn
                    .setAccessible(true);
            java.sql.Connection conn = (java.sql.Connection) dao.getClass()
                    .getDeclaredField("conn")
                    .get(dao);
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
