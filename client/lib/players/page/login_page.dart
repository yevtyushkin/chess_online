import 'package:auto_route/auto_route.dart';
import 'package:client/common/navigation/chess_router.gr.dart';
import 'package:client/common/widgets/non_empty_text_form_field.dart';
import 'package:client/players/state/player_notifier.dart';
import 'package:desktop/desktop.dart';
import 'package:provider/provider.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({Key? key}) : super(key: key);

  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  late PlayerNotifier _playerNotifier;

  @override
  void initState() {
    super.initState();

    _playerNotifier = context.read<PlayerNotifier>();
    _playerNotifier.addListener(_playerNotifierListener);
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const Text(
          'Hi, player. Select your username',
          style: TextStyle(fontSize: 18),
          textAlign: TextAlign.center,
        ),
        NonEmptyTextFormField(
          onSave: _playerNotifier.addPlayer,
          buttonTitle: "let's go!",
          emptyTooltip: 'username cannot be empty',
        ),
      ],
    );
  }

  void _playerNotifierListener() {
    if (_playerNotifier.player != null) {
      AutoRouter.of(context).replace(const Rooms());
    }
  }

  @override
  void dispose() {
    _playerNotifier.removeListener(_playerNotifierListener);
    super.dispose();
  }
}
