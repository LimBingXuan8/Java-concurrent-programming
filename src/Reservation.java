import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Reservation {
    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);

        int numOfSeats;
        int numOfUsers;

        // Prompt user input for number of seats to create and number of users.
        do {
            System.out.println("Enter the number of seats (100-200): \n");
            numOfSeats = sn.nextInt();
            if (numOfSeats < 100 || numOfSeats > 200) {
                System.out.println("The number of seats must be between 100-200.");
            }
        } while (numOfSeats < 100 || numOfSeats > 200);

        System.out.println("Enter the number of users: \n");
        numOfUsers = sn.nextInt();

        sn.close();

        // Create theatre instance
        Theatre theatre = new Theatre(numOfSeats);

        Thread[] threadArray = new Thread[numOfUsers];
        // Create user threads
        for (int i = 0; i < numOfUsers; i++) {
            Thread thread = new Thread(new User(i, theatre));
            threadArray[i] = thread;
            thread.start();
        }
        boolean isAllThreadFinish = false;

        // Wait for all threads to finish executing
        while (!isAllThreadFinish) {
            isAllThreadFinish = true;
            for (int i = 0; i < threadArray.length; i++) {
                isAllThreadFinish = isAllThreadFinish && !threadArray[i].isAlive();
            }
        }

        // Print number of seats available in the theatre, number of users and number of reserved seats
        System.out.println("Number of seats available: " + theatre.getNumOfSeats());
        System.out.println("Number of users in system: " + numOfUsers);
        System.out.println("Number of reserved seats: " + theatre.getNumOfReservedSeats());
    }
}

class Theatre {
    private int numOfSeat;
    private List<Seat> seats;

    Theatre(int numOfSeat) {
        this.numOfSeat = numOfSeat;
        seats = new ArrayList<Seat>();

        //Create seats
        for (int i = 0; i < numOfSeat; i++) {
            seats.add(new Seat(i));
        }

    }

    // Function to find an available seat
    public Seat findAvailableSeat() {
        // Filters the seat list to find a seat which meets the criteria
        Optional<Seat> selectableSeat = seats.stream().filter(seat -> seat.getUnderProcessing() == false &&
                seat.getIsReserved() == false).findFirst();

        // Return the seat if a seat is found
        if (selectableSeat.isPresent()) {
            return selectableSeat.get();
        }

        // Return null if no seats are available.
        return null;
    }

    // Get number of reserved seats
    public int getNumOfReservedSeats() {
        return seats.stream().filter(seat -> seat.getIsReserved() == true).collect(Collectors.toList()).size();
    }

    // Get total number of seats in theatre.
    public int getNumOfSeats() {
        return numOfSeat;
    }

}

class Seat {
    private int seatNumber;
    private User owner;
    private ReentrantLock lock = new ReentrantLock();
    private boolean reserved;
    private boolean underProcessing;
    private int numOfReservedSeats;

    // Seat constructor
    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.numOfReservedSeats = 0;
        this.underProcessing = false;
        this.reserved = false;
        this.owner = null;
    }

    // Function to reserve seat
    public void reserve(User user) {
        try {
            // Sleep for 500 - 1000 ms
            Thread.sleep(getRandomNumber(500, 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Assign an owner to the seat
        this.owner = user;

        // Change seat reserved status to true
        this.reserved = true;
        System.out.println("User-" + user.getId() + " reserved seat-" + seatNumber);
    }

    //Function to get a random number between a min and max value
    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    // Get the underProcessing status of a seat
    public boolean getUnderProcessing() {
        return underProcessing;
    }

    // Select a user and change the underProcessing status to true
    public void selectSeat(User user) {
        this.underProcessing = true;
    }

    // Unselect a user and change the underProcessing status to false
    public void unselectSeat() {
        this.underProcessing = false;
    }

    // Increment number of reserved seats
    public void incrementNumOfSelectedSeats() {
        this.numOfReservedSeats = numOfReservedSeats + 1;
    }

    // Get the reservation status of a seat
    public boolean getIsReserved() {
        return reserved;
    }

    // Get the lock
    public ReentrantLock getLock() {
        return lock;
    }
}

class User implements Runnable {
    // Unique Id for a user
    private int id;

    // Create a theatre instance
    private Theatre theatre;

    // User constructor
    public User(int id, Theatre theatre) {
        this.id = id;
        this.theatre = theatre;
    }

    // Get Id of the user
    public int getId() {
        return id;
    }

    @Override
    public void run() {
        Random rand = new Random();

        // Get a random number between 1-3 as the number of seats to be reserved
        int numOfSeatsToSelect = 1 + rand.nextInt(3);

        // For loop to reserve seats
        for (int i = 0; i < numOfSeatsToSelect; i++) {

            //Find an available seat.
            Seat foundSeat = theatre.findAvailableSeat();

            // Stop if no more seats are available.
            if (foundSeat != null) {
                // Get lock for the available seat
                Lock currentSeatLock = foundSeat.getLock();
                boolean successfullyLocked = false;
                successfullyLocked = currentSeatLock.tryLock();

                if (successfullyLocked) {
                    // Select the seat
                    foundSeat.selectSeat(this);
                    try {
                        //Reserve the seat
                        foundSeat.reserve(this);
                        // Increment the number of reserved seats
                        foundSeat.incrementNumOfSelectedSeats();
                        //Unselect the seat
                        foundSeat.unselectSeat();
                    } finally {
                        // Release the lock
                        currentSeatLock.unlock();
                    }
                }

            } else {
                // Print error message if no seats are available for reservation.
                System.out.println("No seats are available. Failed to reserve");
            }

        }
    }
}



