package no.java.incogito.domain;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.pre.Ord;
import fj.pre.Ordering;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Room {
    public final String name;

    public static final F<String, Room> room_ = new F<String, Room>() {
        public Room f(String name) {
            return new Room(name);
        }
    };

    public static F<Room, String> name_ = new F<Room, String>() {
        public String f(Room room) {
            return room.name;
        }
    };
    public static Ord<Room> ord = Ord.ord(curry(new F2<Room, Room, Ordering>() {
        public Ordering f(Room a, Room b) {
            return Ord.stringOrd.compare(a.name, b.name);
        }
    }));

    public Room(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Room && name.equals(((Room) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                '}';
    }
}
