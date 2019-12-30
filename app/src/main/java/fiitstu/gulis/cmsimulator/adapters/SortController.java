package fiitstu.gulis.cmsimulator.adapters;

import fiitstu.gulis.cmsimulator.models.users.User;

import java.util.Comparator;

public class SortController {
    public static class SortByUsername implements Comparator<User>
    {
        @Override
        public int compare(User o1, User o2) {
            String username1 = o1.getUsername();
            String username2 = o2.getUsername();

            return username1.compareTo(username2);
        }
    }
    public static class SortByLastName implements Comparator<User>
    {
        @Override
        public int compare(User o1, User o2) {
            String lastName1 = o1.getLast_name();
            String lastName2 = o2.getLast_name();

            return lastName1.compareTo(lastName2);
        }
    }

    public static class SortByFirstName implements Comparator<User>
    {
        @Override
        public int compare(User o1, User o2) {
            String firstName1 = o1.getFirst_name();
            String firstName2 = o2.getFirst_name();

            return  firstName1.compareTo(firstName2);
        }
    }
}
