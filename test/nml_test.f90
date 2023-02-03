program p
  implicit none
  character(len=3) :: c0='', c1='', c2='', c3=''
  character(len=1) :: c4(2)='', c5(2)=''
  character(len=1) :: x='x'
  character(len=1) :: c6(3)=''
  character(len=1) :: c7(3)=(/'f', 'o', 'o'/)
  integer :: i0=0, i1=0, i2=0, i3(2)=(/77, 99/), i4(2)=(/77, 77/), i5(4)=(/77, 77, 77, 77/), i6(2)=(/77, 77/), i7=77
  logical :: t0=.false., t1=.false., t2=.false., t3=.false., t4=.false., t5=.false.
  logical :: f0=.true., f1=.true., f2=.true., f3=.true., f4=.true., f5=.true.
  complex :: m0=(0, 0), m1=(0, 0), m2=(0, 0), m3=(0, 0)
  real :: r0(4)=0., r1(4)=0., r2(4)=0., r3(4)=(/66., 77., 88., 99./)
  real :: r12345678901234567890123456789012345678901234567890123456789012=0.
  namelist /na/ c0, c1, c2, c3, c4, c5, c6, c7
  namelist /nb/ i0, i1, i2, i3, i4, i5, i6, i7
  namelist /nc/ t0, t1, t2, t3, t4, t5, f0, f1, f2, f3, f4, f5
  namelist /nd/ m0, m1, m2, m3
  namelist /ne/ r0, r1, r2, r3, r12345678901234567890123456789012345678901234567890123456789012
  namelist /nf/ x
  open (88, file='nl.in', status='old')
  read (88, na)
  read (88, nb)
  read (88, nc)
  read (88, nd)
  read (88, ne)
  read (88, nf)
  close (88)
  if (c0.ne.'foo')                                                           error stop 'FAIL c0'
  if (c1.ne.'BAR')                                                           error stop 'FAIL c1'
  if (c2.ne.'baz')                                                           error stop 'FAIL c2'
  if (c3.ne.'/ &')                                                           error stop 'FAIL c3'
  if (c4(1).ne.'a')                                                          error stop 'FAIL c3(1)'
  if (c4(2).ne.'b')                                                          error stop 'FAIL c3(2)'
  if (c5(1).ne.'c')                                                          error stop 'FAIL c3(1)'
  if (c5(2).ne.'d')                                                          error stop 'FAIL c3(2)'
  if (c6(1).ne.'z')                                                          error stop 'FAIL c6(1)'
  if (c6(2).ne.'z')                                                          error stop 'FAIL c6(2)'
  if (c6(3).ne.'z')                                                          error stop 'FAIL c6(3)'
  if (c7(1).ne.'f')                                                          error stop 'FAIL c7(1)'
  if (c7(2).ne.'o')                                                          error stop 'FAIL c7(2)'
  if (c7(3).ne.'o')                                                          error stop 'FAIL c7(3)'
  if (i0.ne.88)                                                              error stop 'FAIL i0'
  if (i1.ne.88)                                                              error stop 'FAIL i1'
  if (i2.ne.-88)                                                             error stop 'FAIL i2'
  if (i3(1).ne.88)                                                           error stop 'FAIL i3(1)'
  if (i3(2).ne.99)                                                           error stop 'FAIL i3(2)'
  if (i4(1).ne.88)                                                           error stop 'FAIL i4(1)'
  if (i4(2).ne.88)                                                           error stop 'FAIL i4(2)'
  if (i5(1).ne.77)                                                           error stop 'FAIL i5(1)'
  if (i5(2).ne.88)                                                           error stop 'FAIL i5(2)'
  if (i5(3).ne.77)                                                           error stop 'FAIL i5(3)'
  if (i5(4).ne.88)                                                           error stop 'FAIL i5(4)'
  if (i6(1).ne.77)                                                           error stop 'FAIL i6(1)'
  if (i6(2).ne.88)                                                           error stop 'FAIL i6(2)'
  if (i7.ne.77)                                                              error stop 'FAIL i7'
  if (t0.neqv..true.)                                                        error stop 'FAIL t0'
  if (t1.neqv..true.)                                                        error stop 'FAIL t1'
  if (t2.neqv..true.)                                                        error stop 'FAIL t2'
  if (t3.neqv..true.)                                                        error stop 'FAIL t3'
  if (t4.neqv..true.)                                                        error stop 'FAIL t4'
  if (t5.neqv..true.)                                                        error stop 'FAIL t5'
  if (f0.neqv..false.)                                                       error stop 'FAIL f0'
  if (f1.neqv..false.)                                                       error stop 'FAIL f1'
  if (f2.neqv..false.)                                                       error stop 'FAIL f2'
  if (f3.neqv..false.)                                                       error stop 'FAIL f3'
  if (f4.neqv..false.)                                                       error stop 'FAIL f4'
  if (f5.neqv..false.)                                                       error stop 'FAIL f5'
  if (m0.ne.(1., 2.))                                                        error stop 'FAIL m0'
  if (m1.ne.(1.1, 2.2))                                                      error stop 'FAIL m1'
  if (m2.ne.(1.2e3, 2.3d4))                                                  error stop 'FAIL m2'
  if (m3.ne.(-1, .2))                                                        error stop 'FAIL m3'
  if (r0(1).ne.1.)                                                           error stop 'FAIL r0(1)'
  if (r0(2).ne.1.)                                                           error stop 'FAIL r0(2)'
  if (r0(3).ne.-1.)                                                          error stop 'FAIL r0(3)'
  if (r0(4).ne.1.)                                                           error stop 'FAIL r0(4)'
  if (r1(1).ne.1.1e2)                                                        error stop 'FAIL r1(1)'
  if (r1(2).ne.1.1d2)                                                        error stop 'FAIL r1(2)'
  if (r1(3).ne.1.1e-2)                                                       error stop 'FAIL r1(3)'
  if (r1(4).ne.1.1d2)                                                        error stop 'FAIL r1(4)'
  if (r2(1).ne.-1.1e2)                                                       error stop 'FAIL r2(1)'
  if (r2(2).ne.1.1e2)                                                        error stop 'FAIL r2(2)'
! if (r2(3).ne.-1.1d-2)                                                      error stop 'FAIL r2(3)' ! why aren't these equal?
  if (r2(4).ne.1.1d2)                                                        error stop 'FAIL r2(4)'
  if (r3(1).ne.66.)                                                          error stop 'FAIL r3(1)'
  if (r3(2).ne.77.)                                                          error stop 'FAIL r3(2)'
  if (r3(3).ne.88.)                                                          error stop 'FAIL r3(3)'
  if (r3(4).ne.99.)                                                          error stop 'FAIL r3(4)'
  if (r12345678901234567890123456789012345678901234567890123456789012.ne.1.) error stop 'FAIL r1234...0123'
  if (x.ne.'x')                                                              error stop 'FAIL x'
  print *, 'OK'
end program p
