// TODO to be compressed and encrypted (with key.gpg file)

// sign artifacts with key
usePgpKeyHex("<here goes key id>")

// sonatype credentials
credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "s01.oss.sonatype.org",
  "<here goes sonatype username>",
  "<here goes sonatype password>"
)
