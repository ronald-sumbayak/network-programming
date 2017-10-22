class Coba {
    
    public static void main (String args[]) {
        String a = "\\list andrean";
        System.out.println ("> " + a.split ("[@ ]")[0]);
        System.out.println ("> " + a.split ("[@ ]")[1]);
        //System.out.println ("> " + a.split ("[@ ]")[2]);
        //System.out.println ("> " + a.split ("[@ ]")[3]);
        
        String b = "@ronald fafer";
        String command = "";
        String dlimiter = "@";
        System.out.println ("> " + b.split (" ", 2)[0].split (dlimiter)[0]);
        System.out.println ("> " + b.split (" ", 2)[0].split (dlimiter)[1]);
        System.out.println ("> " + b.split (" ", 2)[1]);
        System.out.println ("> " + b.split (String.format ("[%s ]", dlimiter), 3)[0]);
        System.out.println ("> " + b.split (String.format ("[%s ]", dlimiter), 3)[1]);
        System.out.println ("> " + b.split (String.format ("[%s ]", dlimiter), 3)[2]);
    }
}
