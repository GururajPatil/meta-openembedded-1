SUMMARY = "Development package for building Applications that use numa"
DESCRIPTION = "Simple NUMA policy support. It consists of a numactl program \
to run other programs with a specific NUMA policy and a libnuma to do \
allocations with NUMA policy in applications."
LICENSE = "GPL-2.0 & LGPL-2.1"
SECTION = "apps"
RDEPENDS_${PN} = "perl"

inherit autotools-brokensep ptest

LIC_FILES_CHKSUM = "file://README;beginline=19;endline=32;md5=00b9fb4504ff2afcb7fb2a518780f412"

SRC_URI = "ftp://oss.sgi.com/www/projects/libnuma/download/${BPN}-${PV}.tar.gz \
	   file://fix-null-pointer.patch \
	   file://Fix-the-test-output-format.patch \
	   file://Makefile \
	   file://run-ptest \
	   file://0001-define-run-test-target.patch \
          "
SRC_URI[md5sum] = "682c38305b2596967881f3d77bc3fc9c"
SRC_URI[sha256sum] = "dbdac8fe74f13b2e2864bba352f1597ab1d3345c8c485d7805f58d66f414db61"

# ARM does not currently support NUMA
COMPATIBLE_HOST = "^((?!arm).*)$"

do_install() {
        oe_runmake DESTDIR=${D} prefix=${D}/usr install
	#remove the empty man2 directory
	rm -r ${D}${mandir}/man2
}

do_compile_ptest() {
	oe_runmake test
}

do_install_ptest() {
	#install tests binaries
        local test_binaries="distance ftok mbind_mig_pages migrate_pages move_pages \
		mynode	nodemap node-parse pagesize prefered randmap realloc_test \
		tbitmap tshared"

	[ ! -d ${D}/${PTEST_PATH}/test ] && mkdir -p ${D}/${PTEST_PATH}/test
	for i in $test_binaries; do
		install -m 0755 ${B}/test/.libs/$i ${D}${PTEST_PATH}/test
	done

	local test_scripts="checktopology checkaffinity printcpu regress regress2 \
			shmtest  runltp bind_range"
	for i in $test_scripts; do
		install -m 0755 ${B}/test/$i ${D}${PTEST_PATH}/test
	done

	install -m 0755 ${WORKDIR}/Makefile ${D}${PTEST_PATH}/
	install -m 0755 ${B}/.libs/numactl ${D}${PTEST_PATH}/
}

RDEPENDS_${PN}-ptest = "bash"
