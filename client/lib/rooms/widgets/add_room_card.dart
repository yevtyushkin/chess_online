import 'package:client/common/widgets/non_empty_text_form_field.dart';
import 'package:client/rooms/state/rooms_notifier.dart';
import 'package:client/rooms/widgets/room_card.dart';
import 'package:desktop/desktop.dart';
import 'package:provider/provider.dart';

class AddRoomCard extends StatelessWidget {
  const AddRoomCard({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return RoomCard(
      onPressed: () => _openRoomCreationDialog(context),
      title: const Text('Add new room +'),
    );
  }

  void _openRoomCreationDialog(BuildContext context) {
    late final DialogController controller;
    controller = showDialog(
      context,
      builder: (context) {
        return Dialog(
          title: const Text("Please, enter the room name"),
          body: NonEmptyTextFormField(
            onSave: (roomName) => _onAddRoom(context, controller, roomName),
            buttonTitle: 'add',
            emptyTooltip: 'room name cannot be empty',
          ),
        );
      },
    );
  }

  void _onAddRoom(
    BuildContext context,
    DialogController controller,
    String roomName,
  ) {
    context.read<RoomsNotifier>().addRoom(roomName);

    controller.close();
  }
}
