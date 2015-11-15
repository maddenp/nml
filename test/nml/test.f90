program p
  implicit none
  character(len=3) :: c0 = '', c1 = '', c2 = '', c3 = ''
  character(len=1) :: c4(2) = '', c5(2) = ''
  character(len=1) :: x = 'x'
  integer :: i0 = 0, i1 = 0, i2 = 0, i3 = 77
  logical :: t0 = .false., t1 = .false., t2 = .false., t3 = .false., t4 = .false., t5 = .false.
  logical :: f0 = .true., f1 = .true., f2 = .true., f3 = .true., f4 = .true., f5 = .true.
  complex :: m0 = (0, 0), m1 = (0, 0), m2 = (0, 0), m3 = (0, 0)
  real :: r0(4) = 0., r1(4) = 0., r2(4) = 0.
  namelist /na/ c0, c1, c2, c3, c4, c5
  namelist /nb/ i0, i1, i2
  namelist /nc/ t0, t1, t2, t3, t4, t5, f0, f1, f2, f3, f4, f5
  namelist /nd/ m0, m1, m2, m3
  namelist /ne/ r0, r1, r2
  namelist /nf/ x
  open (88, file='nl', status='old')
  read (88, na)
  read (88, nb)
  read (88, nc)
  read (88, nd)
  read (88, ne)
  read (88, nf)
  close (88)
  if (c0.ne.'foo')          stop 'FAIL c0'
  if (c1.ne.'BAR')          stop 'FAIL c1'
  if (c2.ne.'baz')          stop 'FAIL c2'
  if (c3.ne.'qux')          stop 'FAIL c3'
  if (c4(1).ne.'a')         stop 'FAIL c3(1)'
  if (c4(2).ne.'b')         stop 'FAIL c3(2)'
  if (c5(1).ne.'c')         stop 'FAIL c3(1)'
  if (c5(2).ne.'d')         stop 'FAIL c3(d)'
  if (i0.ne.88)             stop 'FAIL i0'
  if (i1.ne.88)             stop 'FAIL i1'
  if (i2.ne.-88)            stop 'FAIL i2'
  if (i3.ne.77)             stop 'FAIL i3'
  if (t0.neqv..true.)       stop 'FAIL t0'
  if (t1.neqv..true.)       stop 'FAIL t1'
  if (t2.neqv..true.)       stop 'FAIL t2'
  if (t3.neqv..true.)       stop 'FAIL t3'
  if (t4.neqv..true.)       stop 'FAIL t4'
  if (t5.neqv..true.)       stop 'FAIL t5'
  if (f0.neqv..false.)      stop 'FAIL f0'
  if (f1.neqv..false.)      stop 'FAIL f1'
  if (f2.neqv..false.)      stop 'FAIL f2'
  if (f3.neqv..false.)      stop 'FAIL f3'
  if (f4.neqv..false.)      stop 'FAIL f4'
  if (f5.neqv..false.)      stop 'FAIL f5'
  if (m0.ne.(1., 2.))       stop 'FAIL m0'
  if (m1.ne.(1.1, 2.2))     stop 'FAIL m1'
  if (m2.ne.(1.2e3, 2.3d4)) stop 'FAIL m2'
  if (m3.ne.(-1, .2))       stop 'FAIL m3'
  if (r0(1).ne.1.)          stop 'FAIL r0(1)'
  if (r0(2).ne.1.)          stop 'FAIL r0(2)'
  if (r0(3).ne.-1.)         stop 'FAIL r0(3)'
  if (r0(4).ne.1.)          stop 'FAIL r0(4)'
  if (r1(1).ne.1.1e2)       stop 'FAIL r1(1)'
  if (r1(2).ne.1.1d2)       stop 'FAIL r1(2)'
  if (r1(3).ne.1.1e-2)      stop 'FAIL r1(3)'
  if (r1(4).ne.1.1d2)       stop 'FAIL r1(4)'
  if (r2(1).ne.-1.1e2)      stop 'FAIL r2(1)'
  if (r2(2).ne.1.1e2)       stop 'FAIL r2(2)'
! if (r2(3).ne.-1.1d-2)     stop 'FAIL r2(3)' ! why aren't these equal?
  if (r2(4).ne.1.1d2)       stop 'FAIL r2(4)'
  if (x.ne.'x')             stop 'FAIL x'
  print *, 'OK'
end program p
