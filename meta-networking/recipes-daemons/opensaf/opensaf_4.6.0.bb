SUMMARY = "OpenSAF is an open source implementation of the SAF AIS specification"
DESCRIPTION = "OpenSAF is an open source project established to develop a base platform \
middleware consistent with Service Availability Forum (SA Forum) \
specifications, under the LGPLv2.1 license. The OpenSAF Foundation was \
established by leading Communications and Enterprise Computing Companies to \
facilitate the OpenSAF Project and to accelerate the adoption of the OpenSAF \
code base in commercial products. \
The OpenSAF project was launched in mid 2007 and has been under development by \
an informal group of supporters of the OpenSAF initiative. The OpenSAF \
Foundation was founded on January 22nd 2008 with Emerson Network Power, \
Ericsson, Nokia Siemens Networks, HP and Sun Microsystems as founding members."
HOMEPAGE = "http://www.opensaf.org"

inherit autotools useradd systemd pkgconfig

SRC_URI = "${SOURCEFORGE_MIRROR}/${BPN}/releases/${BPN}-${PV}.tar.gz \
           file://install-samples-from-srcdir.patch \
           file://plmcd.service \
           file://plmcboot.service \
           file://0001-plmcd-error-fix.patch \
           file://Revert_imma_client_node_replyPending_to_unsigned_char.patch \
           file://Fix_GCC_5.1.0_compiler_warning.patch \
           "

SRC_URI[md5sum] = "a1ceddb517c0972aa7e899b092d7f464"
SRC_URI[sha256sum] = "f6ea754a145ae42552f328f96c32f38429ad06478a351b9d0aed812adcecb2b4"

SECTION = "admin"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=a916467b91076e631dd8edb7424769c7"

DEPENDS = "libxml2 python"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "-f -r opensaf"
USERADD_PARAM_${PN} =  "-r -g opensaf -d ${datadir}/opensaf/ -s ${sbindir}/nologin -c \"OpenSAF\" opensaf"

SYSTEMD_SERVICE_${PN} += "opensafd.service plmcboot.service plmcd.service"
SYSTEMD_AUTO_ENABLE = "disable"

PACKAGECONFIG[systemd] = "--enable-systemd-daemon"
PACKAGECONFIG[openhpi] = "--with-hpi-interface=B03 --enable-ais-plm,,openhpi"

do_configure_prepend () {
        ( cd ${S}; autoreconf -f -i -s )
}

EXTRA_OECONF += " --libdir=${libdir}/opensaf "
EXTRA_OEMAKE += " -Wl,-rpath,${libdir}/opensaf "

PKGLIBDIR="${libdir}/opensaf/opensaf"

FILES_${PN} += "${localstatedir}/run"

FILES_${PN}-staticdev += "${PKGLIBDIR}/*.a"

RDEPENDS_${PN} += "bash python"

INSANE_SKIP_${PN} = "dev-so"

do_install_append() {
    rm -fr "${D}${localstatedir}/lock"
    rm -fr "${D}${localstatedir}/run"
    rmdir --ignore-fail-on-non-empty "${D}${localstatedir}"
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${B}/osaf/services/infrastructure/nid/config/opensafd.service \
        ${D}${systemd_unitdir}/system
    install -m 644 ${WORKDIR}/plmc*.service ${D}/${systemd_unitdir}/system
    sed -i -e 's#@SYSCONFDIR@#${sysconfdir}#g' ${D}${systemd_unitdir}/system/plmc*.service
    sed -i -e 's#@SBINDIR@#${sbindir}#g' ${D}${systemd_unitdir}/system/plmc*.service

}
