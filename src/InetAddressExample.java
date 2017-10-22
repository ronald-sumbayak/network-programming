import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

class InetAddressExample {
    
    private void findHostAddress () {
        Scanner scanner = new Scanner (System.in);
        System.out.println ("Host name: ");
        String host = scanner.next ();
        findHostAddress (host);
    }
    
    private void findHostAddress (String host) {
        try {
            InetAddress address = java.net.InetAddress.getByName (host);
            System.out.println ("IP Address of "
                                + host
                                + " is "
                                + address.getHostAddress ());
        }
        catch (UnknownHostException e) {
            System.out.println ("Could not find host " + host);
            e.printStackTrace (System.out);
        }
    }
    
    private void findLocaHostAddress () {
        try {
            InetAddress address = InetAddress.getLocalHost ();
            System.out.println ("Local Host IP: " + address.getHostAddress ());
        }
        catch (UnknownHostException e) {
            System.out.println ("Could not find Local Host IP");
            e.printStackTrace (System.out);
        }
    }
    
    public static void main (String[] args) {
        InetAddressExample inet = new InetAddressExample ();
        inet.findLocaHostAddress ();
        inet.findHostAddress ("google.com");
        inet.findHostAddress ();
    }
}
