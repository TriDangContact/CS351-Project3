// java program to demonstrate 
// use of semaphores Locks
import java.util.concurrent.*;
import java.util.Random;
 
//A shared resource/class.
class Shared 
{
    static int count = 500;
    static int returns = 0;
    static int soldA = 0;
    static int soldB = 0;
    static int soldC = 0;
    static int buyers = 0;
}
 
class MyThread extends Thread
{
    Semaphore sem;
    String threadName;
    public MyThread(Semaphore sem, String threadName) 
    {
        super(threadName);
        this.sem = sem;
        this.threadName = threadName;
    }
 
    @Override
    public void run() {
        final int RETURN_RATE = 11;          //1 out of N, rate of users returning tickets
        final int SLEEP_RATE = 5;          //1 out of N, rate of user just browsing
        final int TICKET_COUNT = Shared.count;       //max ticket count
        final int TICKET_RETURN = 20;       //max number of ticket return thread allowed
        final int TICKET_RELEASE = 3;       //number of tickets that can be bought during one thread
        // create instance of Random class
        Random rand = new Random();
        int randTickets;
        int randSleep;
        int randReturn;
        int ticketsSold = 0;
        
        //sellers keep on selling tickets if there is still available
        while (Shared.count > 0) {
        //System.out.println("Starting " + threadName);
            try
            {
                // First, try to browse.
                // acquiring the lock
                sem.acquire();
                Shared.buyers++;
                System.out.println(threadName + " is browsing tickets");
                // Simulate time for various user activity
                randomDelay();
                // Now, accessing the shared resource.
                // other waiting threads will wait, until this 
                // thread release the lock

                //if there's no more tickets
                if (Shared.count == 0) {
                    return;
                }
                //if there's still tickets left
                else {
                    //simulate 1 in 20 browser will exceed browsing limit
                    randSleep = rand.nextInt(SLEEP_RATE);
                    randReturn = rand.nextInt(RETURN_RATE);
                    if (randSleep == 0) {
                        System.out.println(threadName + " browsed for more than 2 minutes. Returning to pool!");
                    }
                    //simulate 1 in 5 browser will return 1 ticket
                    else if (randReturn == 0) {
                        if (Shared.returns < TICKET_RETURN && Shared.count < TICKET_COUNT ) {
                            Shared.count++;
                            Shared.returns++;
                            System.out.println(threadName + " is RETURNING 1 ticket. Tickets left: " +Shared.count);
                        }
                        //System.out.println(threadName+ " tried to RETURN tickets.");
                    }
                    else {
                        // Generate random integers in range 1 to 3 to simulate num of tickets user wants to buy
                        randTickets = rand.nextInt(TICKET_RELEASE) + 1;
                        //if there's still tickets left
                        if ((Shared.count - randTickets) >= 0) {
                            Shared.count -= randTickets;
                            ticketsSold += randTickets;
                            System.out.println("Tickets SOLD by thread " +threadName+ ": " +randTickets+ 
                                    ". Tickets left: " + Shared.count);
                            
                        }
                        //if there's buying more tickets than available
                        else {
                            System.out.println("NOT ENOUGH tickets available! "
                                    + "Tried to sell: " +randTickets+ ". Tickets left: " + Shared.count);
                        }
                    }
                }
            } catch (InterruptedException exc) {
                    System.out.println(exc);
                }
            // Release the lock.
            //System.out.println(threadName + " is no longer buying");
            if (threadName.equals("A")) {
                Shared.soldA = ticketsSold;
            }
            else if (threadName.equals("B")) {
                Shared.soldB = ticketsSold;
            }
            else {
                Shared.soldC = ticketsSold;
            }
            System.out.println(threadName+ " total tickets sold: " +ticketsSold);
            sem.release();
        }
        System.out.println("No more tickets!");
    }
    
    public void randomDelay() {
        try {
            Random rand = new Random(); 
            int randDelay = rand.nextInt(200);
            Thread.sleep(randDelay);
        } catch (InterruptedException exc){
            System.out.println(exc);
        }
    }
}

 
// Driver class
public class TicketSeller 
{
    public static void main(String args[]) throws InterruptedException 
    {
        // creating a Semaphore object
        // with number of permits 3
        Semaphore sem = new Semaphore(3);
         
        // creating 3 threads with name A, B, C
        
        MyThread mt1 = new MyThread(sem, "A");
        MyThread mt2 = new MyThread(sem, "B");
        MyThread mt3 = new MyThread(sem, "C");
        
        
        // starting threads
        mt1.start();
        mt2.start();
        mt3.start();
        // waiting for threads
        mt1.join();
        mt2.join();
        mt3.join();
        
        // all threads will complete their execution
        System.out.println("Done! Final ticket count: " + Shared.count);
        
        System.out.println("Total tickets sold by A: " +Shared.soldA);
        System.out.println("Total tickets sold by B: " +Shared.soldB);
        System.out.println("Total tickets sold by C: " +Shared.soldC);
        int sold = Shared.soldA + Shared.soldB + Shared.soldC;
        System.out.println("Total tickets sold: " +sold+ ". Total returned: " +Shared.returns+ ". Total Buyers: " +Shared.buyers);
    }
}