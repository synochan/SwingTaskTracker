import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to add sample movies to the database.
 * Compile and run this file separately to add the movies.
 */
public class add_sample_movies {
    public static void main(String[] args) {
        try {
            // Get database connection
            Connection connection = com.cinebook.dao.DBConnection.getConnection();
            
            // Check if movies exist
            boolean hasMovies = false;
            try (Statement stmt = connection.createStatement()) {
                hasMovies = stmt.executeQuery("SELECT COUNT(*) FROM movies").getInt(1) > 0;
            }
            
            // Add sample movies if none exist
            if (!hasMovies) {
                addSampleMovies(connection);
                System.out.println("Sample movies added successfully!");
            } else {
                System.out.println("Movies already exist in the database.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void addSampleMovies(Connection connection) throws SQLException {
        String[] sampleMovies = {
            "INSERT INTO movies (title, director, cast, genre, synopsis, duration_minutes, rating, release_date, poster_url, is_active) " +
            "VALUES ('Inception', 'Christopher Nolan', 'Leonardo DiCaprio, Ellen Page, Ken Watanabe', 'Sci-Fi, Action', " +
            "'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.', " +
            "148, 'PG-13', '2010-07-16', 'https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_.jpg', 1)",
            
            "INSERT INTO movies (title, director, cast, genre, synopsis, duration_minutes, rating, release_date, poster_url, is_active) " +
            "VALUES ('The Dark Knight', 'Christopher Nolan', 'Christian Bale, Heath Ledger, Aaron Eckhart', 'Action, Crime, Drama', " +
            "'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.', " +
            "152, 'PG-13', '2008-07-18', 'https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_.jpg', 1)",
            
            "INSERT INTO movies (title, director, cast, genre, synopsis, duration_minutes, rating, release_date, poster_url, is_active) " +
            "VALUES ('Parasite', 'Bong Joon Ho', 'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong', 'Drama, Thriller', " +
            "'Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.', " +
            "132, 'R', '2019-05-30', 'https://m.media-amazon.com/images/M/MV5BYWZjMjk3ZTItODQ2ZC00NTY5LWE0ZDYtZTI3MjcwN2Q5NTVkXkEyXkFqcGdeQXVyODk4OTc3MTY@._V1_.jpg', 1)",
            
            "INSERT INTO movies (title, director, cast, genre, synopsis, duration_minutes, rating, release_date, poster_url, is_active) " +
            "VALUES ('Avengers: Endgame', 'Anthony Russo, Joe Russo', 'Robert Downey Jr., Chris Evans, Mark Ruffalo', 'Action, Adventure, Drama', " +
            "'After the devastating events of Avengers: Infinity War, the universe is in ruins. With the help of remaining allies, the Avengers assemble once more in order to reverse Thanos actions and restore balance to the universe.', " +
            "181, 'PG-13', '2019-04-26', 'https://m.media-amazon.com/images/M/MV5BMTc5MDE2ODcwNV5BMl5BanBnXkFtZTgwMzI2NzQ2NzM@._V1_.jpg', 1)",
            
            "INSERT INTO movies (title, director, cast, genre, synopsis, duration_minutes, rating, release_date, poster_url, is_active) " +
            "VALUES ('Coco', 'Lee Unkrich, Adrian Molina', 'Anthony Gonzalez, Gael García Bernal, Benjamin Bratt', 'Animation, Adventure, Family', " +
            "'Aspiring musician Miguel, confronted with his family`s ancestral ban on music, enters the Land of the Dead to find his great-great-grandfather, a legendary singer.', " +
            "105, 'PG', '2017-11-22', 'https://m.media-amazon.com/images/M/MV5BYjQ5NjM0Y2YtNjZkNC00ZDhkLWJjMWItN2QyNzFkMDE3ZjAxXkEyXkFqcGdeQXVyODIxMzk5NjA@._V1_.jpg', 1)"
        };
        
        // Create screenings for each movie
        String[] sampleScreenings = {
            "INSERT INTO screenings (movie_id, cinema_id, screening_time, standard_seat_price, deluxe_seat_price, is_active) " +
            "VALUES (1, 1, datetime('now', '+1 day', 'start of day', '+18 hours'), 180.00, 280.00, 1)",
            
            "INSERT INTO screenings (movie_id, cinema_id, screening_time, standard_seat_price, deluxe_seat_price, is_active) " +
            "VALUES (1, 2, datetime('now', '+1 day', 'start of day', '+20 hours'), 200.00, 300.00, 1)",
            
            "INSERT INTO screenings (movie_id, cinema_id, screening_time, standard_seat_price, deluxe_seat_price, is_active) " +
            "VALUES (2, 3, datetime('now', '+2 day', 'start of day', '+19 hours'), 190.00, 290.00, 1)",
            
            "INSERT INTO screenings (movie_id, cinema_id, screening_time, standard_seat_price, deluxe_seat_price, is_active) " +
            "VALUES (3, 1, datetime('now', '+2 day', 'start of day', '+17 hours'), 180.00, 280.00, 1)",
            
            "INSERT INTO screenings (movie_id, cinema_id, screening_time, standard_seat_price, deluxe_seat_price, is_active) " +
            "VALUES (4, 2, datetime('now', '+3 day', 'start of day', '+18 hours'), 200.00, 300.00, 1)",
            
            "INSERT INTO screenings (movie_id, cinema_id, screening_time, standard_seat_price, deluxe_seat_price, is_active) " +
            "VALUES (5, 3, datetime('now', '+3 day', 'start of day', '+16 hours'), 180.00, 280.00, 1)"
        };
        
        // Execute movie insertion
        try (Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            
            // Add movies
            for (String sql : sampleMovies) {
                stmt.executeUpdate(sql);
            }
            
            // Add screenings
            for (String sql : sampleScreenings) {
                stmt.executeUpdate(sql);
            }
            
            // Generate seats for each screening
            for (int screeningId = 1; screeningId <= sampleScreenings.length; screeningId++) {
                // Get cinema details for this screening
                int cinemaId = 0;
                try (PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT cinema_id FROM screenings WHERE id = ?")) {
                    pstmt.setInt(1, screeningId);
                    cinemaId = pstmt.executeQuery().getInt("cinema_id");
                }
                
                // Get cinema configuration
                int totalRows = 0;
                int seatsPerRow = 0;
                boolean hasDeluxeSeats = false;
                try (PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT total_rows, seats_per_row, has_deluxe_seats FROM cinemas WHERE id = ?")) {
                    pstmt.setInt(1, cinemaId);
                    var resultSet = pstmt.executeQuery();
                    totalRows = resultSet.getInt("total_rows");
                    seatsPerRow = resultSet.getInt("seats_per_row");
                    hasDeluxeSeats = resultSet.getInt("has_deluxe_seats") == 1;
                }
                
                // Generate seats
                generateSeats(connection, screeningId, totalRows, seatsPerRow, hasDeluxeSeats);
            }
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    private static void generateSeats(Connection connection, int screeningId, int totalRows, int seatsPerRow, boolean hasDeluxeSeats) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO seats (screening_id, seat_number, seat_type, is_reserved, row_number, column_number) VALUES (?, ?, ?, 0, ?, ?)")) {
            
            // Define the rows using letters (A, B, C, ...)
            char[] rowLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            
            for (int row = 0; row < totalRows; row++) {
                for (int col = 0; col < seatsPerRow; col++) {
                    String seatNumber = rowLetters[row] + String.valueOf(col + 1);
                    
                    // Determine seat type (last 2 rows are deluxe if cinema has deluxe seats)
                    String seatType = "STANDARD";
                    if (hasDeluxeSeats && row >= totalRows - 2) {
                        seatType = "DELUXE";
                    }
                    
                    pstmt.setInt(1, screeningId);
                    pstmt.setString(2, seatNumber);
                    pstmt.setString(3, seatType);
                    pstmt.setInt(4, row + 1);
                    pstmt.setInt(5, col + 1);
                    pstmt.executeUpdate();
                }
            }
        }
    }
}