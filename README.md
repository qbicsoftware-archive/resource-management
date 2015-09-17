facs (developement name)
======

NOT ready for production.

Everything was written in Java 1.7 openjdk, [vaadin](https://vaadin.com/hom://vaadin.com/home) 7.5, [Liferay 6.2](https://www.liferay.com/),
10.0.20-MariaDB

Servlets/Portlets build to mangage or schedule users to scientific resources.

Many work groups have different measurement instruments. And different
operators/users with a different level of expertiece for different instruments.

The idea of this tool is to enable workgroup leaders to manage the
accesibility of resource and to enable or disable different users.

There are several Servlets/Portlets for achieving this goal:

resource, instrument, device are used interchangebly. Called it whatever you
like.

Calendar
-----
For each resource there is a calendar.
Resources can be restricted, which means, that users can reserve a time slot for
using that device, but administrators have to confirm that time slot, or remove
it.
Administrators can set for users groups, e.g. 'beginner' that they are
able to reserve a time slots on restricited schedules, e.g. from monday to
friday, 8 am to 4 pm.

It should syncronise with the database so that changes will be displayed
immediately.


Statistics
-----
show user and usage statiistics. Might get fancy, but for now it is a filterable
table.


Billing
-----
usage of instruments costs money. Create bills from statistics and device
information (see upload).



Upload
-----
devices sometimes produce statistics about their usage. It is quite useful to
see how long users used a devices. e.g. if it says in the calendar that they are
going to use it for 1 hour on a sunday but use it for 5 hours, because nobody was there except for them, one can still bill 5 hours instead
of one.

It is not entirely trivial to match uploaded user names with the user names of
the portal, because they might be totally different. Therefore manual
interaction is needed for now.


Settings
-----
add, modify, delete,lock resources, users, costs etc.



Database
-----
This one is not a portlet.
The database could now be mysql or mariadb. Other sql-like databases should work
as well, but have not been tested.

The database class is now implemented as a ?static singelton?.
The reason is that the whole thing runs as a bunch of liferay portlets. 
If one users logs in and tries to reserve an instrument through the calendar,
all other users should be immediately notified. If the database class is a static
singelton, all users use the same instance.
The database class is supposed to send events to listeners that a calendar was
updated. And the calendar portlet should listen to that and update its calendar
immediately.

Other than that all portlets use that class to insert, update or delete values (calendar events, user names. instrument prices etc.).



