# hue_lights_controller_app

Arnes Respati Putri, Tine Jozelj, Max Walhagen, Simon Ohrberg, Carl-Håkan Hovstadius, Zorica Ilievska

This is a final project for Internet of Things and People Course.

## Background

More and more devices are getting connected to the internet and there is a need for smart ways for them to communicate. This is the domain of pervasive computing which expands on mobile computing. In our project we have used Arduino, an open source  electronic prototyping platform, to control three Philips hue light bulbs. We have developed an android app that can find the lamps via Beacons. This app can then control the Arduino using MQTT, Message Queuing Telemetry Transport, protocol. This is the data-oriented approach meaning using a language that all of the devices can be made to understand. In our case we used the REST protocol. 

## Goals and Methods

The goal of this project is to control three Philips Hue light bulbs separately and unanimously via an application that we developed. There are three beacon simulators, where each one has a unique ID and is acting as a light bulb. The application thus can find the light bulbs via beacons and can control the Arduino Uno using MQTT protocol. The application should be connecting to a beacon, using a phone will provide the ability to reduce the number of available objects with which to interact. The application is limited to connect only to the three specified IDs.

We use the application to communicate with the beacons. The beacons used in this project are virtual beacons, represented by a couple of smartphones to illustrate how the technology would work. In a practical scenario the beacons would be the lights themselves. If a beacon or beacons are detected by the application We’re given access to pass data into a MQTT broker. This data will eventually affect the hue lights through the broker, the Arduino that is subscribing to a topic and sending the data to the gateway that is controlling the lights.
