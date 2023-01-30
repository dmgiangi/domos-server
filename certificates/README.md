# Certificates
## Certificate generation
### Create a root certificate key
In order to have a unique root certificate,so you can easily add it in your truststore and verify all the certificate that you need:
We will generate a key for our root certificate with openSSL as follows:
```shell
openssl genrsa -des3 -out root.key 2048 
```
the password used for the key in this repo is: password
### Create a root certificate
Create a certificate with the key previously created:
```shell
openssl req -new -x509 -days 64000 -key root.key -out root.crt
```
### Create a certificate
With the following command you can generate a certificate that you can use to secure your ssl/tsl connection. \
_**it seems that using the exact same data (e.g. State, City) in both certificates can cause problems with some software.**_ \
I usually replace FQDN with the domain that I'm going to protect, so I can take track of the domain where i can use this certificate.
```shell
# Generate unencrypted key with: 
openssl genrsa -out FQDN.key 2048
# Generate a certificate request
openssl req -new -out FQDN.csr -key FQDN.key
# Generate the signed certificate with
openssl x509 -req -in FQDN.csr -CA root.crt -CAkey root.key -CAcreateserial -out FQDN.crt -days 64000
```
### Now?
you don't need usually the csr file, so you can delete it. \
you can now install your certificate where you need.
## Certificate Installation
### Chrome installation
Depending on the version of chrome the installation procedure can change, but usually is simple.
you need only to import the root.crt file in the chrome trust store
### Nginx installation
for Nginx, you need to create a file that contains the entire certification chain.
This is easy as follows:
```shell
cat FQDN.crt root.crt >> bundle.crt
```
ou should also create a strong Diffie-Hellman group, which is used in negotiating Perfect Forward Secrecy with clients.
```shell
openssl dhparam -out dhparam.pem 2048
```
then add in your nginx configuration the following line:
```
server {
    listen 443 http2 ssl;
    listen [::]:443 http2 ssl;

    server_name FQDN;

    ssl_certificate /path/to/bundle.crt;
    ssl_certificate_key /path/to/FDQN.key;
    ssl_dhparam /path/to/dhparam.pem;
    
    ...
    other configuration
    ...
}
```

keytool -import -file ./root.crt -alias MyRootCa -keystore testTruststore
