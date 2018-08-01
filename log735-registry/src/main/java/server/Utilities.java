package server;

public class Utilities {

    public static long ipToLong(String ipAddress) {


        String[] ipAddressInArray = ipAddress.split("\\.");

        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);

        }

        return result;
    }

    public static long ipAndPortToLong(String ip, int port){
        return Long.valueOf(port) + ipToLong(ip);
    }
}
