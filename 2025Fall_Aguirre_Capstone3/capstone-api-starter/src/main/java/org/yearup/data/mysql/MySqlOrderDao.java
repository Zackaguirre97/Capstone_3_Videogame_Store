package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    /*
     * Constructor
     * */
    public MySqlOrderDao(DataSource dataSource){
        super(dataSource);
    }

    /*
    * Methods
    * */
    @Override
    public int createOrder(int userId,
                           LocalDateTime date,
                           String address,
                           String city,
                           String state,
                           String zip,
                           BigDecimal shippingAmount)
    {
        String sql = """
        INSERT INTO orders
        (user_id, date, address, city, state, zip, shipping_amount)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement =
                    connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            statement.setInt(1, userId);
            statement.setObject(2, date);
            statement.setString(3, address);
            statement.setString(4, city);
            statement.setString(5, state);
            statement.setString(6, zip);
            statement.setBigDecimal(7, shippingAmount);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0)
            {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next())
                {
                    return generatedKeys.getInt(1); // order_id
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return -1;
    }

    @Override
    public void addLineItem(int orderId,
                            int productId,
                            BigDecimal salesPrice,
                            int quantity,
                            BigDecimal discount)
    {
        String sql =
                "INSERT INTO order_line_items " +
                        "(order_id, product_id, sales_price, quantity, discount) " +
                        "VALUES (?, ?, ?, ?, ?);";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setBigDecimal(3, salesPrice);
            statement.setInt(4, quantity);
            statement.setBigDecimal(5, discount);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
