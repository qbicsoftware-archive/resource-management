Flowcytometry Booking Portlets (FACS)
======
*Many work groups have different measurement instruments. And different
operators/users with a different level of expertise for different instruments.*

The idea of this tool is to enable workgroup leaders to manage the
accessibility of resources as well as the users which want to use those
resources.

The project consists of several Servlets/Portlets which should help to mangage or schedule users and scientific resources.

Quick Setup
=====
1. <code>git clone https://github.com/qbicsoftware/resource-management.git</code>
1. Install IvyDE, including Ant Tasks, if needed
   (http://www.apache.org/dist/ant/ivyde/updatesite)
1. Insall vaadin see here:
   https://vaadin.com/book/-/page/getting-started.environment.html#getting-started.environment.eclipse
1. Install Liferay 6.2+
1. Import the project into Eclipse
1. create war
1. deploy war file in Liferay
1. create database with [link
   here](https://github.com/qbicsoftware/resource-management-data)


Everything was written in Java 1.7 openjdk, [vaadin](https://vaadin.com/hom://vaadin.com/home) 7.5, [Liferay 6.2](https://www.liferay.com/),
10.0.20-MariaDB

There are several Servlets/Portlets for achieving this goal:
(resource, instrument, device are used).

Calendar
-----
For each resource there is a calendar.
Resources can be restricted, It means, that users can reserve a time slot for
a resource, but administrators have to confirm the time slot or remove
it from the calendar if they have good reason.
In resources which are not restricted users just reserve a time slot with no
interaction by administrators.

Additionally, Administrators set time blocks in which a group of users, e.g. beginners can use a resource.
For example, a group leader wants to ensure that a device is only used by the
group beginners if an advanced users is around. That could mean that beginners
are only allowed to reserve time slots from Monday to
Friday, 8 am to 4 pm.

Note: It should synchronise with the database so that changes will be displayed
immediately.


Statistics
-----
Show user and usage statistics. Might get fancy, but for now it is a filtered
table or vaadin grid.


Billing
-----
Usage of resources costs money. Create bills from statistics and device
information (see upload).



Upload
-----
Devices sometimes produce statistics about their usage. It is quite useful to
see how long users used a devices. e.g. if it says in the calendar that they are
going to use it for 1 hour on a sunday but use it for 5 hours, because nobody was there except for them, one can still bill 5 hours instead
of one.

It is not entirely trivial to match uploaded user names with the user names of
the portal, because they might be totally different. Therefore manual
interaction is needed for now.


Settings
-----
Add, modify, delete, lock resources, users, costs etc.



Database
-----
This one is not a portlet.
The database could now be a Mysql or Mariadb. Other SQL-like databases should work
as well, but have not been tested.

The database class is now implemented as a ?static singelton?.
The reason is that the whole thing runs as a bunch of liferay portlets. 
If one user logs in and tries to reserve an instrument through the calendar,
all other users should be immediately notified. If the database class is a static
singelton, all users use the same instance, because it exists only once for the
JVM.
The database class is supposed to send 'calendar changed' events (and others) to listeners. The calendar portlet should listen to that and update itself
immediately.

Other than that all portlets use that class to insert, update or delete values (calendar events, user names. instrument prices etc.).



