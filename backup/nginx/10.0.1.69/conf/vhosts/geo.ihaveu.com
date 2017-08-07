geo $remote_addr $up_num {
	default 2 ;
	210.12.69.48/28  3 ;
	10.0.0.0/8       3 ;
}
